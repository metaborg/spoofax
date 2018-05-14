package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.util.Collection;
import java.util.List;

import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LayoutSensitivePrettyPrinterPrimitive extends AbstractPrimitive {
    private static final ILogger logger = LoggerUtils.logger(LayoutSensitivePrettyPrinterPrimitive.class);

    @Inject private static Provider<ISpoofaxLanguageSpecService> languageSpecServiceProvider;

    private final IProjectService projectService;

    @Inject public LayoutSensitivePrettyPrinterPrimitive(IProjectService projectService) {
        super("SSL_EXT_apply_layout_constraints_pp", 0, 0);

        this.projectService = projectService;
    }

    private SetMultimap<Integer, EditInformation> edits;
    private ITermFactory tf;

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        IStrategoAppl topmostBox = (IStrategoAppl) env.current(); // Should be V([], box)
        tf = env.getFactory();
        edits = HashMultimap.create();

        IStrategoTerm result = checkForConstraints(topmostBox);
        // System.out.println(result);

        env.setCurrent(result);
        return true;
    }


    private IStrategoTerm checkForConstraints(IStrategoTerm term) {

        IStrategoTerm result = term;

        if(term instanceof IStrategoAppl) {
            String constructorName = ((IStrategoAppl) term).getConstructor().getName();

            // H(_, box*) box or V(_, box*) box or Z(_, box*) box
            if(constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z")) {
                IStrategoTerm boxes = result.getSubterm(1);
                IStrategoTerm newBoxes = boxes;

                if(boxes.getAnnotations() != null) {
                    for(IStrategoTerm t : boxes.getAnnotations()) {
                        if(t instanceof IStrategoAppl
                            && ((IStrategoAppl) t).getConstructor().getName().equals("Align")) {
                            newBoxes = applyAlignConstraint(newBoxes, t);
                            result = annotateTerm(
                                tf.makeAppl(((IStrategoAppl) term).getConstructor(), term.getSubterm(0), newBoxes),
                                term.getAnnotations());
                            result = updateColumnBoxes(result, getPosition(result));
                            newBoxes = result.getSubterm(1);
                        }
                    }
                }
            }
        }


        IStrategoTerm[] subTerms = new IStrategoTerm[result.getSubtermCount()];

        for(int i = 0; i < result.getSubtermCount(); i++) {
            subTerms[i] = checkForConstraints(result.getSubterm(i));
        }

        if(result instanceof IStrategoAppl) {
            return tf.copyAttachments(term,
                tf.makeAppl(((IStrategoAppl) result).getConstructor(), subTerms, term.getAnnotations()));
        } else if(result instanceof IStrategoList) {
            return tf.copyAttachments(term, tf.makeList(subTerms, term.getAnnotations()));
        } else {
            return result;
        }
    }


    private IStrategoTerm applyAlignConstraint(IStrategoTerm t, IStrategoTerm constraint) {
        IStrategoTerm ref = constraint.getSubterm(0);
        IStrategoTerm targ = constraint.getSubterm(1).getSubterm(0);

        IStrategoTerm posRef = getPositionRef(t, ref, true);
        IStrategoTerm termTarg = getTermFromSelector(t, targ, true);

        return applyAlignConstraintToSelector(t, posRef, termTarg, ref);
    }


    private IStrategoTerm applyAlignConstraintToSelector(IStrategoTerm t, IStrategoTerm posRef, IStrategoTerm termTarg,
        IStrategoTerm ref) {

        if(t.equals(termTarg)) {
            // Pos(_, <id>)
            int refCol = ((IStrategoInt) posRef.getSubterm(1)).intValue();

            IStrategoTerm posTarg = getPosition(termTarg);
            assert (posTarg != null);
            int targCol = ((IStrategoInt) posTarg.getSubterm(1)).intValue();
            int targLine = ((IStrategoInt) posTarg.getSubterm(0)).intValue();

            posTarg = getPosition(termTarg);
            targCol = ((IStrategoInt) posTarg.getSubterm(1)).intValue();
            targLine = ((IStrategoInt) posTarg.getSubterm(0)).intValue();

            if(targCol > refCol) {
                // wrap in Z box to jump to next line and wrap into H box to add X spaces
                // Z([], [S(""), H([SOpt(HS(), X)], [S(""), t])])

                int indentationSpaces = countIndentation(termTarg, 0);

                edits.put(targLine, new EditInformation(targCol, -(targCol - refCol)));
                System.out.println("In Line " + targLine + " after column " + targCol + " shifted boxes by "
                    + -(targCol - refCol) + " spaces.");

                IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                IStrategoTerm hBoxConfig = tf.makeAppl(tf.makeConstructor("SOpt", 2),
                    tf.makeAppl(tf.makeConstructor("HS", 0)), tf.makeString("" + (refCol - 1 - indentationSpaces)));
                IStrategoTerm hBox =
                    tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hBoxConfig), tf.makeList(emptyBox, termTarg));

                return tf.makeAppl(tf.makeConstructor("Z", 2), tf.makeList(), tf.makeList(emptyBox, hBox));
            } else if(targCol < refCol) {
                // wrap in H box to add spaces
                // H([SOpt(HS(), "dif")], [S(""), t])

                edits.put(targLine, new EditInformation(targCol, refCol - targCol));
                System.out.println("In Line " + targLine + " after column " + targCol + " shifted boxes by "
                    + (refCol - targCol) + " spaces.");

                IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                IStrategoTerm hBoxConfig = tf.makeAppl(tf.makeConstructor("SOpt", 2),
                    tf.makeAppl(tf.makeConstructor("HS", 0)), tf.makeString("" + (refCol - targCol)));

                return tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hBoxConfig),
                    tf.makeList(emptyBox, termTarg));

            } else {
                return termTarg;
            }


        }

        IStrategoTerm[] subTerms = new IStrategoTerm[t.getSubtermCount()];

        for(int i = 0; i < t.getSubtermCount(); i++) {
            subTerms[i] = applyAlignConstraintToSelector(t.getSubterm(i), posRef, termTarg, ref);
        }

        if(t instanceof IStrategoAppl) {
            return tf.copyAttachments(t,
                tf.makeAppl(((IStrategoAppl) t).getConstructor(), subTerms, t.getAnnotations()));
        } else if(t instanceof IStrategoList) {
            return tf.copyAttachments(t, tf.makeList(subTerms, t.getAnnotations()));
        }

        return t;

    }

    private IStrategoTerm getTermFromSelector(IStrategoTerm t, IStrategoTerm selector, boolean topmost) {
        if(t.getAnnotations() != null) {
            boolean foundAnotherConstraint = false;
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(anno.equals(selector)) {
                    return t;
                }
                if(anno instanceof IStrategoAppl && ((IStrategoAppl) anno).getConstructor().getName().equals("Align")) {
                    foundAnotherConstraint = true;
                }
            }
            if(foundAnotherConstraint && !topmost) {
                return null;
            }
        }

        IStrategoTerm term = null;

        for(IStrategoTerm sub : t.getAllSubterms()) {
            term = getTermFromSelector(sub, selector, false);
            if(term != null) {
                return term;
            }
        }

        return null;
    }


    private IStrategoTerm getPosition(IStrategoTerm t) {
        if(t.getAnnotations() != null) {
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(anno instanceof IStrategoAppl
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                    return anno;
                }
            }
        }

        return null;
    }

    private IStrategoTerm getPositionRef(IStrategoTerm t, IStrategoTerm ref, boolean topmost) {
        if(t.getAnnotations() != null) {
            IStrategoTerm pos = null;
            boolean foundRef = false;
            boolean foundAnotherConstraint = false;
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(anno.equals(ref)) {
                    if(pos != null) {
                        return pos;
                    } else {
                        foundRef = true;
                    }
                }
                if(anno instanceof IStrategoAppl
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                    pos = anno;
                    if(foundRef) {
                        return anno;
                    }
                }
                if(anno instanceof IStrategoAppl && ((IStrategoAppl) anno).getConstructor().getName().equals("Align")) {
                    foundAnotherConstraint = true;
                }
            }
            if(foundAnotherConstraint && !topmost) {
                return null;
            }
        }

        IStrategoTerm pos = null;

        for(IStrategoTerm sub : t.getAllSubterms()) {
            pos = getPositionRef(sub, ref, false);
            if(pos != null) {
                return pos;
            }
        }

        return null;
    }

    private IStrategoTerm updateHorizontalListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position, int hs) {
        IStrategoTerm updatedPosition = shiftColumn(getEndPosition(head, position), hs);

        List<IStrategoTerm> newList = Lists.newArrayList();

        newList.add(head);
        newList.addAll(updateColumnBoxesHorizontal(updatedPosition, hs, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        if(firstPositionBox != null) {
            return annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            return tf.annotateTerm(newTerm, t.getAnnotations());
        }
    }

    private IStrategoTerm updateColumnBoxes(IStrategoTerm t, IStrategoTerm position) {
        if(t instanceof IStrategoAppl) {
            // S(_)
            if(((IStrategoAppl) t).getConstructor().getName().equals("S")) {
                IStrategoString string = (IStrategoString) t.getSubterm(0);
                if(string.stringValue().length() != 0) {
                    return annotateBoxPosition(t, position, t.getAnnotations());
                } else {
                    return t;
                }
            }

            // H(_, []) or V(_, []) or Z(_, [])
            if(t.getSubterm(1).getSubtermCount() == 0) {
                return t;
            }

            IStrategoTerm head = updateColumnBoxes(t.getSubterm(1).getSubterm(0), position);
            List<IStrategoTerm> tail = Lists.newArrayList();

            for(int i = 1; i < t.getSubterm(1).getAllSubterms().length; i++) {
                tail.add(t.getSubterm(1).getSubterm(i));
            }

            // H box
            if(((IStrategoAppl) t).getConstructor().getName().equals("H")) {
                // H([], [head | tail])

                if(t.getSubterm(0).getSubtermCount() == 0) {

                    return updateHorizontalListOfBoxes(t, head, tail, position, 1);
                } else { // H([SOpt(HS(), hs)], [b | bs])
                    IStrategoTerm horizontalSpace = t.getSubterm(0).getSubterm(0).getSubterm(1);
                    assert horizontalSpace instanceof IStrategoString;
                    int hs = Integer.parseInt(((IStrategoString) horizontalSpace).stringValue());

                    return updateHorizontalListOfBoxes(t, head, tail, position, hs);
                }
            }
            // V box
            if(((IStrategoAppl) t).getConstructor().getName().equals("V")) {
                // V([], [head | tail])
                if(t.getSubterm(0).getSubtermCount() == 0) {
                    return updateVerticalListOfBoxes(t, head, tail, position, 1);
                } else { // V([SOpt(VS(), vs)], [b | bs])
                    IStrategoTerm verticalSpace = t.getSubterm(0).getSubterm(1);
                    assert verticalSpace instanceof IStrategoString;
                    int vs = Integer.parseInt(((IStrategoString) verticalSpace).stringValue());

                    return updateVerticalListOfBoxes(t, head, tail, position, vs);
                }
            }
            // Z box
            if(((IStrategoAppl) t).getConstructor().getName().equals("Z")) {
                return updateVerticalZListOfBoxes(t, head, tail, position);
            }


        }

        return null;
    }


    private IStrategoTerm updateVerticalZListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position) {
        IStrategoTerm updatedPosition = shiftLine(getEndPosition(head, position), 1);

        List<IStrategoTerm> newList = Lists.newArrayList();

        newList.add(head);
        newList.addAll(updateColumnBoxesVerticalZ(updatedPosition, 1, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        if(firstPositionBox != null) {
            return annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            return tf.annotateTerm(newTerm, t.getAnnotations());
        }
    }


    private IStrategoTerm updateVerticalListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position, int vs) {
        IStrategoTerm column = position.getSubterm(1);
        IStrategoTerm updatedPosition = shiftLine(getEndPosition(head, position), vs, column);

        List<IStrategoTerm> newList = Lists.newArrayList();

        newList.add(head);
        newList.addAll(updateColumnBoxesVertical(updatedPosition, vs, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        if(firstPositionBox != null) {
            return annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            return tf.annotateTerm(newTerm, t.getAnnotations());
        }
    }


    private IStrategoTerm shiftColumn(IStrategoTerm position, int hs) {
        return tf.makeAppl(tf.makeConstructor("Position", 2), position.getSubterm(0),
            tf.makeInt(((IStrategoInt) position.getSubterm(1)).intValue() + hs));
    }


    private IStrategoTerm shiftLine(IStrategoTerm position, int vs) {
        if(vs == 0) {
            return position;
        } else {
            return tf.makeAppl(tf.makeConstructor("Position", 2),
                tf.makeInt(((IStrategoInt) position.getSubterm(0)).intValue() + vs), tf.makeInt(1));
        }
    }

    private IStrategoTerm shiftLine(IStrategoTerm position, int vs, IStrategoTerm column) {
        return tf.makeAppl(tf.makeConstructor("Position", 2),
            tf.makeInt(((IStrategoInt) position.getSubterm(0)).intValue() + vs), column);
    }


    private IStrategoTerm getEndPosition(IStrategoTerm t, IStrategoTerm position) {
        if(t instanceof IStrategoAppl) {
            // S box
            if(((IStrategoAppl) t).getConstructor().getName().equals("S")) {
                IStrategoString text = (IStrategoString) t.getSubterm(0);

                int column = ((IStrategoInt) position.getSubterm(1)).intValue();

                return tf.makeAppl(tf.makeConstructor("Position", 2), position.getSubterm(0),
                    tf.makeInt(column + text.stringValue().length()));
            }
            // H box
            if(((IStrategoAppl) t).getConstructor().getName().equals("H")) {
                if(t.getSubterm(0).getSubtermCount() == 0) {
                    return getEndPositionHList(t.getSubterm(1), position, 1);
                } else {
                    IStrategoTerm horizontalSpace = t.getSubterm(0).getSubterm(0).getSubterm(1);
                    assert horizontalSpace instanceof IStrategoString;

                    int hs = Integer.parseInt(((IStrategoString) horizontalSpace).stringValue());
                    return getEndPositionHList(t.getSubterm(1), position, hs);
                }
            }
            // V box
            if(((IStrategoAppl) t).getConstructor().getName().equals("V")) {
                if(t.getSubterm(0).getSubtermCount() == 0) {
                    return getEndPositionVList(t.getSubterm(1), position, 1);
                } else {
                    IStrategoTerm verticalSpace = t.getSubterm(0).getSubterm(0).getSubterm(1);
                    assert verticalSpace instanceof IStrategoString;

                    int vs = Integer.parseInt(((IStrategoString) verticalSpace).stringValue());
                    return getEndPositionVList(t.getSubterm(1), position, vs);
                }
            }
            if(((IStrategoAppl) t).getConstructor().getName().equals("Z")) {
                return getEndPositionZList(t.getSubterm(1), position);
            }
        }
        return position;
    }


    private IStrategoTerm getEndPositionHList(IStrategoTerm t, IStrategoTerm position, int hs) {
        assert t instanceof IStrategoList;

        if(t.getSubtermCount() == 0) {
            return position;
        }

        if(t.getSubtermCount() == 1) {
            return getEndPosition(t.getSubterm(0), position);
        }

        IStrategoTerm endPosition = getEndPosition(((IStrategoList) t).head(), position);

        return getEndPositionHList(((IStrategoList) t).tail(), shiftColumn(endPosition, hs), hs);
    }


    private IStrategoTerm getEndPositionVList(IStrategoTerm t, IStrategoTerm position, int vs) {
        assert t instanceof IStrategoList;

        if(t.getSubtermCount() == 0) {
            return position;
        }

        if(t.getSubtermCount() == 1) {
            return getEndPosition(t.getSubterm(0), position);
        }

        IStrategoTerm column = position.getSubterm(1);
        IStrategoTerm endPosition = getEndPosition(((IStrategoList) t).head(), position);

        return getEndPositionVList(((IStrategoList) t).tail(), shiftLine(endPosition, vs, column), vs);

    }


    private IStrategoTerm getEndPositionZList(IStrategoTerm t, IStrategoTerm position) {
        assert t instanceof IStrategoList;

        if(t.getSubtermCount() == 0) {
            return position;
        }

        if(t.getSubtermCount() == 1) {
            return getEndPosition(t.getSubterm(0), position);
        }

        IStrategoTerm endPosition = getEndPosition(((IStrategoList) t).head(), position);

        return getEndPositionZList(((IStrategoList) t).tail(), shiftLine(endPosition, 1));

    }


    private IStrategoTerm annotateBoxPosition(IStrategoTerm t, IStrategoTerm position, IStrategoList oldAnnotations) {
        List<IStrategoTerm> newAnnotations = Lists.newArrayList();

        for(IStrategoTerm anno : oldAnnotations) {
            if(anno instanceof IStrategoAppl && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                continue;
            }
            newAnnotations.add(anno);
        }
        newAnnotations.add(position);
        return tf.annotateTerm(t, tf.makeList(newAnnotations.toArray(new IStrategoTerm[newAnnotations.size()])));
    }


    private List<IStrategoTerm> updateColumnBoxesHorizontal(IStrategoTerm position, int hs, List<IStrategoTerm> list) {

        List<IStrategoTerm> result = Lists.newArrayList();

        if(list.isEmpty()) {
            return result;
        }

        IStrategoTerm head = updateColumnBoxes(list.get(0), position);

        result.add(head);

        IStrategoTerm endPosition = shiftColumn(getEndPosition(head, position), hs);

        result.addAll(updateColumnBoxesHorizontal(endPosition, hs, list.subList(1, list.size())));

        return result;
    }

    private Collection<? extends IStrategoTerm> updateColumnBoxesVertical(IStrategoTerm position, int vs,
        List<IStrategoTerm> list) {

        List<IStrategoTerm> result = Lists.newArrayList();

        if(list.isEmpty()) {
            return result;
        }

        IStrategoTerm head = updateColumnBoxes(list.get(0), position);

        result.add(head);

        IStrategoTerm column = position.getSubterm(1);
        IStrategoTerm endPosition = shiftLine(getEndPosition(head, position), vs, column);

        result.addAll(updateColumnBoxesVertical(endPosition, vs, list.subList(1, list.size())));

        return result;
    }

    private Collection<? extends IStrategoTerm> updateColumnBoxesVerticalZ(IStrategoTerm position, int vs,
        List<IStrategoTerm> list) {
        List<IStrategoTerm> result = Lists.newArrayList();

        if(list.isEmpty()) {
            return result;
        }

        IStrategoTerm head = updateColumnBoxes(list.get(0), position);

        result.add(head);

        IStrategoTerm endPosition = shiftLine(getEndPosition(head, position), vs);

        result.addAll(updateColumnBoxesHorizontal(endPosition, vs, list.subList(1, list.size())));

        return result;
    }

    private IStrategoTerm annotateTerm(IStrategoTerm t, IStrategoList annotations) {
        return tf.annotateTerm(t, annotations);
    }

    private int countIndentation(IStrategoTerm t, int indentation) {
        
        return 0;
//        // H box
//        if(t instanceof IStrategoAppl && ((IStrategoAppl) t).getConstructor().getName().equals("H")) {
//            // H([], _)
//            if(t.getSubterm(0).getSubtermCount() == 0) {
//                // if head of list is S(_){Position(x, y)}
//                // return indentation
//                // if head of list is S("")
//                // return countIndentation(tail, indentation+1)
//                // if is head = box{Position(x, y)}
//                // return countIndentation(head, indentation)
//                // else
//                // return countIndentation(tail, countIndentation(head, indentation)+1)
//            } else {
//                // if head of list is S(_){Position(x, y)}
//                // return indentation
//                // if head of list is S("")
//                // return countIndentation(tail, indentation+hs)
//                // if is head = box{Position(x, y)}
//                // return countIndentation(head, indentation)
//                // else
//                // return countIndentation(tail, countIndentation(head, indentation)+hs)
//            }
//        }
//        // V box
//        if(t instanceof IStrategoAppl && ((IStrategoAppl) t).getConstructor().getName().equals("V")) {
//            // V([], _)
//            // if head of list is S(_){Position(x, y)}
//            // return indentation
//            // if head of list is S("")
//            // return countIndentation(tail, indentation)
//            // if is head = box{Position(x, y)}
//            // return countIndentation(head, indentation)
//            // else
//            // return countIndentation(tail, indentation)
//        }
//        // Z box
//        if(t instanceof IStrategoAppl && ((IStrategoAppl) t).getConstructor().getName().equals("Z")) {
//         // Z([], _)
//            // if head of list is S(_){Position(x, y)}
//            // return indentation
//            // if head of list is S("")
//            // return countIndentation(tail, indentation)
//            // if is head = box{Position(x, y)}
//            // return countIndentation(head, indentation)
//            // else
//            // return countIndentation(tail, indentation)
//        }
//        // S box
//        if(t instanceof IStrategoAppl && ((IStrategoAppl) t).getConstructor().getName().equals("S")) {
//            return indentation;
//        }
//        // return accumulated indentation
//
//        return indentation;
    }

    private IStrategoTerm findFirstPosition(IStrategoTerm t) {
        if(t.getAnnotations() != null) {
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(anno instanceof IStrategoAppl
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                    return anno;
                }
            }
        }

        IStrategoTerm position = null;
        for(IStrategoTerm subterm : t.getAllSubterms()) {
            position = findFirstPosition(subterm);
            if(position != null) {
                return position;
            }
        }

        return null;

    }
}
