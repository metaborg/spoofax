package org.metaborg.spoofax.meta.core.stratego.primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.util.collection.SetMultimap;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import javax.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class LayoutSensitivePrettyPrinterPrimitive extends AbstractPrimitive {
    @Inject public LayoutSensitivePrettyPrinterPrimitive() {
        super("SSL_EXT_apply_layout_constraints_pp", 0, 0);
    }

    private ITermFactory tf;

    private boolean foundOffside;

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) throws InterpreterException {
        IStrategoAppl topmostBox = (IStrategoAppl) env.current(); // Should be V([], box)
        tf = env.getFactory();

        /*
         * For an offside box which has more characters in the same line where it ends move the remaining characters to
         * a new line
         */
        foundOffside = false;
        IStrategoTerm newTopMostBox = isolateOffsideBoxes(topmostBox);

        IStrategoTerm result = checkForConstraints(newTopMostBox);

        env.setCurrent(result);
        return true;
    }


    private IStrategoTerm isolateOffsideBoxes(IStrategoTerm t) {

        SetMultimap<Integer, IStrategoTerm> line2Box = new SetMultimap<>();
        createMappingLine2Box(t, line2Box);

        // create a map of line -> box
        // for all offside boxes, check if there is a box that starts in the same line in a column higher
        // if so, put next box after the offside box in a new line

        IStrategoTerm positionWithLayout = getPositionWithLayout(t);
        IStrategoTerm newBox = checkOffsideBoxes(t, line2Box);

        return updateColumnBoxes(newBox, positionWithLayout);
    }


    private void createMappingLine2Box(IStrategoTerm t, SetMultimap<Integer, IStrategoTerm> line2Box) {
        if(TermUtils.isAppl(t)) {
            String constructorName = ((IStrategoAppl) t).getConstructor().getName();

            if(constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z")) {

                IStrategoTerm position = getPosition(t);
                if(position != null) {
                    int line = TermUtils.toJavaIntAt(position, 0);
                    line2Box.put(line, t);
                }

                for(IStrategoTerm subBox : t.getSubterm(1)) {
                    createMappingLine2Box(subBox, line2Box);
                }
            }

            if(constructorName.equals("S")) {
                IStrategoTerm position = getPosition(t);
                if(position != null) {
                    int line = TermUtils.toJavaIntAt(position, 0);
                    line2Box.put(line, t);
                }
            }
        }
    }


    private IStrategoTerm checkOffsideBoxes(IStrategoTerm t, SetMultimap<Integer, IStrategoTerm> line2Box) {

        IStrategoTerm result = t;

        IStrategoTerm[] subTerms = new IStrategoTerm[result.getSubtermCount()];

        if(TermUtils.isAppl(t)) {
            String constructorName = ((IStrategoAppl) t).getConstructor().getName();
            if(constructorName.equals("S") && foundOffside) {
                String value = TermUtils.toJavaStringAt(t, 0);
                if(!value.trim().isEmpty()) {
                    foundOffside = false;
                    return tf.makeAppl(tf.makeConstructor("Z", 2), tf.makeList(),
                        tf.makeList(tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString("")), result));
                }
            } else if(foundOffside
                && (constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z"))) {
                foundOffside = false;
                return tf.makeAppl(tf.makeConstructor("Z", 2), tf.makeList(), tf.makeList(
                    tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString("")), checkOffsideBoxes(result, line2Box)));

            }
        }

        for(int i = 0; i < result.getSubtermCount(); i++) {
            subTerms[i] = checkOffsideBoxes(result.getSubterm(i), line2Box);
        }

        if(TermUtils.isAppl(t)) {
            String constructorName = ((IStrategoAppl) t).getConstructor().getName();

            if(constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z")) {

                IStrategoTerm boxes = result.getSubterm(1);

                if(boxes.getAnnotations() != null) {
                    for(IStrategoTerm anno : boxes.getAnnotations()) {
                        // Offside(_, [_ | _])
                        if(TermUtils.isAppl(anno)
                            && ((IStrategoAppl) anno).getConstructor().getName().equals("Offside")
                            && anno.getSubterm(1).getSubtermCount() != 0) {
                            if(hasFollowingBox(result, line2Box)) {
                                foundOffside = true;
                            }
                        }

                    }
                }

                for(IStrategoTerm anno : t.getAnnotations()) {
                    // Offside(_, [])
                    if(TermUtils.isAppl(anno)
                        && ((IStrategoAppl) anno).getConstructor().getName().equals("Offside")
                        && anno.getSubterm(1).getSubtermCount() == 0) {
                        if(hasFollowingBox(result, line2Box)) {
                            foundOffside = true;
                        }
                    }
                }
            }
        }

        if(TermUtils.isAppl(result)) {
            result = annotateTerm(tf.makeAppl(((IStrategoAppl) result).getConstructor(), subTerms), t.getAnnotations());
        } else if(TermUtils.isList(result)) {
            result = annotateTerm(tf.makeList(subTerms), t.getAnnotations());
        }

        return result;

    }


    private boolean hasFollowingBox(IStrategoTerm t, SetMultimap<Integer, IStrategoTerm> line2Box) {
        IStrategoTerm pos = getEndPosition(t, getPositionWithLayout(t));
        if(pos == null) {
            return false;
        }

        int line = TermUtils.toJavaIntAt(pos, 0);
        int col = TermUtils.toJavaIntAt(pos, 1);

        for(IStrategoTerm box : line2Box.get(line)) {
            @Nullable IStrategoTerm posBox = getPosition(box);
            if (posBox == null) continue;
            int boxCol = TermUtils.toJavaIntAt(posBox, 1);
            if(boxCol > col) {
                return true;
            }
        }

        return false;
    }


    private IStrategoTerm checkForConstraints(IStrategoTerm term) {

        IStrategoTerm result = term;

        if(TermUtils.isAppl(term)) {
            String constructorName = ((IStrategoAppl) term).getConstructor().getName();

            // H(_, box*) box or V(_, box*) box or Z(_, box*) box
            if(constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z")) {
                IStrategoTerm boxes = result.getSubterm(1);
                IStrategoTerm newBoxes = boxes;

                if(boxes.getAnnotations() != null) {
                    for(IStrategoTerm t : boxes.getAnnotations()) {
                        // Offside(_, [_ | _])
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("Offside")
                            && t.getSubterm(1).getSubtermCount() != 0) {
//                            System.out.println("Applying constraint " + t);
                            newBoxes = applyOffsideConstraint(newBoxes, t);
                            result = annotateTerm(
                                tf.makeAppl(((IStrategoAppl) term).getConstructor(), term.getSubterm(0), newBoxes),
                                term.getAnnotations());
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }

                        // Align(_, [_ | _])
                        if(TermUtils.isAppl(t) && ((IStrategoAppl) t).getConstructor().getName().equals("Align")
                            && t.getSubtermCount() == 2) {
//                            System.out.println("Applying constraint " + t);
                            newBoxes = applyAlignConstraint(newBoxes, t);
                            result = annotateTerm(
                                tf.makeAppl(((IStrategoAppl) term).getConstructor(), term.getSubterm(0), newBoxes),
                                term.getAnnotations());
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }


                        // Indent(_, [_ | _])
                        if(TermUtils.isAppl(t) && ((IStrategoAppl) t).getConstructor().getName().equals("Indent")
                            && t.getSubterm(1).getSubtermCount() != 0) {
//                            System.out.println("Applying constraint " + t);
                            newBoxes = applyIndentConstraint(newBoxes, t);
                            result = annotateTerm(
                                tf.makeAppl(((IStrategoAppl) term).getConstructor(), term.getSubterm(0), newBoxes),
                                term.getAnnotations());
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }

                        // NewLineIndent(_, [_ | _])
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("NewLineIndent")
                            && t.getSubterm(1).getSubtermCount() != 0) {
//                            System.out.println("Applying constraint " + t);
                            result = applyNewLineIndentConstraint(result, t, 1);
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }

                        // NewLineIndentBy(_, _, [_ | _])
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("NewLineIndentBy")) {
//                            System.out.println("Applying constraint " + t);
                            result =
                                applyNewLineIndentConstraint(result, t, TermUtils.toJavaIntAt(t, 0));
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }
                        
                     // NewLine(_)
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("NewLine")) {
//                            System.out.println("Applying constraint " + t);
                            result = applyNewLineConstraint(result, t, 0);
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }

                        // NewLineBy(_, _)
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("NewLineBy")) {
//                            System.out.println("Applying constraint " + t);
                            result =
                                applyNewLineConstraint(result, t, TermUtils.toJavaIntAt(t, 0));
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                            newBoxes = result.getSubterm(1);
                        }
                    }
                    
                    for(IStrategoTerm t : term.getAnnotations()) {
                        // Offside(_, [])
                        if(TermUtils.isAppl(t)
                            && ((IStrategoAppl) t).getConstructor().getName().equals("Offside")
                            && t.getSubterm(1).getSubtermCount() == 0) {
//                            System.out.println("Applying constraint " + t);
                            result = annotateTerm(applyOffsideTreeConstraint(result), term.getAnnotations());
                            result = updateColumnBoxes(result, getPositionWithLayout(result));
                        }
                    }
                }

            }
            
            if(constructorName.equals("V")) {
                for(IStrategoTerm t : term.getAnnotations()) {
                    // Align(_)
                    if(TermUtils.isAppl(t) && ((IStrategoAppl) t).getConstructor().getName().equals("Align")
                        && t.getSubtermCount() == 1) {
//                        System.out.println("Applying constraint " + t);
                        IStrategoTerm newBoxes = applyAlignListConstraint(result.getSubterm(1));
                        result = annotateTerm(
                            tf.makeAppl(((IStrategoAppl) term).getConstructor(), term.getSubterm(0), newBoxes),
                            term.getAnnotations());
                        result = updateColumnBoxes(result, getPositionWithLayout(result));
                    }
                }
            }

        }


        IStrategoTerm[] subTerms = new IStrategoTerm[result.getSubtermCount()];

        for(int i = 0; i < result.getSubtermCount(); i++) {
            subTerms[i] = checkForConstraints(result.getSubterm(i));
        }

        if(TermUtils.isAppl(result)) {
            return tf.copyAttachments(term,
                tf.makeAppl(((IStrategoAppl) result).getConstructor(), subTerms, term.getAnnotations()));
        } else if(TermUtils.isList(result)) {
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

        return applyAlignConstraintToSelector(t, posRef, termTarg);
    }


    private IStrategoTerm applyAlignListConstraint(IStrategoTerm boxes) {
        if(boxes.getSubtermCount() == 0) {
            return boxes;
        }

        IStrategoTerm[] newBoxes = new IStrategoTerm[boxes.getSubtermCount()];
        IStrategoTerm posRef = getPosition(boxes.getSubterm(0));

        newBoxes[0] = boxes.getSubterm(0);
        for(int i = 1; i < boxes.getSubtermCount(); i++) {
            IStrategoTerm currentBox = boxes.getSubterm(i);
            // in case a box in the list has changed because of the offside newline
            // apply the constraint in the inner box
            if(TermUtils.isAppl(currentBox)) {
                if(((IStrategoAppl) currentBox).getConstructor().getName().equals("Z")) {
                    IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                    if(currentBox.getSubterm(1).getSubtermCount() == 2
                        && currentBox.getSubterm(1).getSubterm(0).equals(emptyBox)) {
                        IStrategoTerm innerbox = currentBox.getSubterm(1).getSubterm(1);
                        newBoxes[i] = annotateTerm(
                            tf.makeAppl(((IStrategoAppl) currentBox).getConstructor(), currentBox.getSubterm(0),
                                tf.makeList(emptyBox, applyAlignConstraintToSelector(innerbox, posRef, innerbox))),
                            currentBox.getAnnotations());
                        continue;
                    }
                }
            }
            newBoxes[i] = applyAlignConstraintToSelector(currentBox, posRef, currentBox);
        }

        return annotateTerm(tf.makeList(newBoxes), boxes.getAnnotations());
    }


    private IStrategoTerm applyIndentConstraint(IStrategoTerm t, IStrategoTerm constraint) {
        IStrategoTerm ref = constraint.getSubterm(0);
        IStrategoTerm targ = constraint.getSubterm(1).getSubterm(0);

        IStrategoTerm posRef = getPositionRef(t, ref, true);
        IStrategoTerm termTarg = getTermFromSelector(t, targ, true);

        // if not in a column further already, align it with a column higher than posRef
        IStrategoTerm posTarg = getPosition(termTarg);

        int colRef = TermUtils.toJavaIntAt(posRef, 1);
        int colTarg = TermUtils.toJavaIntAt(posTarg, 1);

        if(colTarg <= colRef) {
            return applyAlignConstraintToSelector(t, shiftColumn(posRef, 1), termTarg);
        } else {
            return t;
        }
    }


    private IStrategoTerm applyNewLineIndentConstraint(IStrategoTerm t, IStrategoTerm constraint, int size) {
        IStrategoTerm ref, targ;
        if(((IStrategoAppl) constraint).getConstructor().getName().equals("NewLineIndentBy")) {
            ref = constraint.getSubterm(1);
            targ = constraint.getSubterm(2).getSubterm(0);
        } else {
            ref = constraint.getSubterm(0);
            targ = constraint.getSubterm(1).getSubterm(0);
        }

        IStrategoTerm posRef = getPositionRef(t.getSubterm(1), ref, true);
        IStrategoTerm termTarg = getTermFromSelector(t.getSubterm(1), targ, true);

        IStrategoTerm posTarg = getPosition(termTarg);

        int lineRef = TermUtils.toJavaIntAt(posRef, 0);
        int lineTarg = TermUtils.toJavaIntAt(posTarg, 0);

        // if target term is not in a line greater than the reference term
        if(lineRef >= lineTarg) {
            t = moveToNextLine(t, termTarg);
            t = updateColumnBoxes(t, getPositionWithLayout(t));
            termTarg = getTermFromSelector(t.getSubterm(1), targ, true);
            posTarg = getPosition(termTarg);
        }

        // if not in a column further already, align it with a column higher than posRef
        int colRef = TermUtils.toJavaIntAt(posRef, 1);
        int colTarg = TermUtils.toJavaIntAt(posTarg, 1);

        if(colTarg <= colRef) {
            return applyAlignConstraintToSelector(t, shiftColumn(posRef, size), termTarg);
        } else {
            return t;
        }
    }


    private IStrategoTerm applyNewLineConstraint(IStrategoTerm t, IStrategoTerm constraint, int size) {
        IStrategoTerm targ;
        
        if(((IStrategoAppl) constraint).getConstructor().getName().equals("NewLine")) {
            targ = constraint.getSubterm(0);
        } else {           
            targ = constraint.getSubterm(1);
        }
        
        IStrategoTerm termTarg = getTermFromSelector(t.getSubterm(1), targ, true);
        IStrategoTerm posTarg = getPosition(termTarg);
        
        int colTarg = TermUtils.toJavaIntAt(posTarg, 1);
        
        if(colTarg != 1) {
            t = moveToNextLine(t, termTarg);
            t = updateColumnBoxes(t, getPositionWithLayout(t));
            termTarg = getTermFromSelector(t.getSubterm(1), targ, true);
            posTarg = getPosition(termTarg);
        }
        
        return applyAlignConstraintToSelector(t, shiftColumn(posTarg, size), termTarg);
    }


    private IStrategoTerm moveToNextLine(IStrategoTerm t, IStrategoTerm termTarg) {
        if(t.equals(termTarg)) {
            // wrap in Z box to jump to next line
            // Z([], [S(""), t])
            IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
            return tf.makeAppl(tf.makeConstructor("Z", 2), tf.makeList(), tf.makeList(emptyBox, t));
        }

        IStrategoTerm[] subTerms = new IStrategoTerm[t.getSubtermCount()];

        for(int i = 0; i < t.getSubtermCount(); i++) {
            subTerms[i] = moveToNextLine(t.getSubterm(i), termTarg);
        }

        if(TermUtils.isAppl(t)) {
            return tf.copyAttachments(t,
                tf.makeAppl(((IStrategoAppl) t).getConstructor(), subTerms, t.getAnnotations()));
        } else if(TermUtils.isList(t)) {
            return tf.copyAttachments(t, tf.makeList(subTerms, t.getAnnotations()));
        }

        return t;
    }


    private IStrategoTerm applyOffsideConstraint(IStrategoTerm t, IStrategoTerm constraint) {
        IStrategoTerm ref = constraint.getSubterm(0);
        IStrategoTerm targ = constraint.getSubterm(1).getSubterm(0);

        IStrategoTerm posRef = getPositionRef(t, ref, true);
        IStrategoTerm termTarg = getTermFromSelector(t, targ, true);

        return applyOffsideConstraintToSelector(t, posRef, termTarg);
    }


    private IStrategoTerm applyOffsideTreeConstraint(IStrategoTerm t) {
        IStrategoTerm posRef = getPosition(t);
        if(posRef != null) {
            return applyOffsideConstraintToZBoxes(t, posRef);
        } else {
            return t;
        }

    }


    private IStrategoTerm applyAlignConstraintToSelector(IStrategoTerm t, IStrategoTerm posRef,
        IStrategoTerm termTarg) {

        if(t.equals(termTarg)) {
            // Pos(_, <id>)
            int refCol = TermUtils.toJavaIntAt(posRef, 1);

            IStrategoTerm posTarg = getPosition(termTarg);
            assert (posTarg != null);
            int targCol = TermUtils.toJavaIntAt(posTarg, 1);

            posTarg = getPosition(termTarg);
            targCol = TermUtils.toJavaIntAt(posTarg, 1);

            if(targCol > refCol) {
                // wrap in Z box to jump to next line and wrap into H box to indent X spaces
                // Z([], [S(""), H([SOpt(HS(), X)], [S(""), t])])

                // remove the indentation that is there already
                termTarg = removeCurrentIndentation(termTarg);

//                System.out.println("In Line " + targLine + " after column " + targCol + " shifted boxes by "
//                    + -(targCol - refCol) + " spaces.");

                IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                IStrategoTerm hBoxConfig = tf.makeAppl(tf.makeConstructor("SOpt", 2),
                    tf.makeAppl(tf.makeConstructor("HS", 0)), tf.makeString("" + (refCol - 1)));
                IStrategoTerm hBox =
                    tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hBoxConfig), tf.makeList(emptyBox, termTarg));

                return tf.makeAppl(tf.makeConstructor("Z", 2), tf.makeList(), tf.makeList(emptyBox, hBox));
            } else if(targCol < refCol) {
                // wrap in H box to add spaces
                // H([SOpt(HS(), "dif")], [S(""), t])
                // all inner Z boxes should also be indented

//                System.out.println("In Line " + targLine + " after column " + targCol + " shifted boxes by "
//                    + (refCol - targCol) + " spaces.");

                IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                IStrategoTerm hBoxConfig = tf.makeAppl(tf.makeConstructor("SOpt", 2),
                    tf.makeAppl(tf.makeConstructor("HS", 0)), tf.makeString("" + (refCol - targCol)));

                return tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hBoxConfig),
                    tf.makeList(emptyBox, indentZboxes(termTarg, hBoxConfig)));

            } else {
//                System.out.println("Didn't change anything.");
                return termTarg;
            }


        }

        IStrategoTerm[] subTerms = new IStrategoTerm[t.getSubtermCount()];

        for(int i = 0; i < t.getSubtermCount(); i++) {
            subTerms[i] = applyAlignConstraintToSelector(t.getSubterm(i), posRef, termTarg);
        }

        if(TermUtils.isAppl(t)) {
            return tf.copyAttachments(t,
                tf.makeAppl(((IStrategoAppl) t).getConstructor(), subTerms, t.getAnnotations()));
        } else if(TermUtils.isList(t)) {
            return tf.copyAttachments(t, tf.makeList(subTerms, t.getAnnotations()));
        }

        return t;

    }

    private IStrategoTerm indentZboxes(IStrategoTerm t, IStrategoTerm hBoxConfig) {
        if(TermUtils.isAppl(t)) {
            String constructorName = ((IStrategoAppl) t).getConstructor().getName();

            if(constructorName.equals("H") || constructorName.equals("V")) {
                List<IStrategoTerm> newBoxes = new ArrayList<>();
                IStrategoTerm boxes = t.getSubterm(1);
                for(IStrategoTerm subTerm : boxes) {
                    newBoxes.add(indentZboxes(subTerm, hBoxConfig));
                }
                return annotateTerm(
                    tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
                        tf.makeList(newBoxes.toArray(new IStrategoTerm[newBoxes.size()])), boxes.getAnnotations())),
                    t.getAnnotations());
            }

            if(constructorName.equals("Z")) {
                List<IStrategoTerm> newBoxes = new ArrayList<>();
                IStrategoTerm boxes = t.getSubterm(1);
                if(boxes.getSubtermCount() != 0) {
                    newBoxes.add(indentZboxes(boxes.getSubterm(0), hBoxConfig));
                    for(int i = 1; i < boxes.getSubtermCount(); i++) {
                        IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                        newBoxes.add(tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hBoxConfig),
                            tf.makeList(emptyBox, indentZboxes(boxes.getSubterm(i), hBoxConfig))));
                    }
                }

                return annotateTerm(
                    tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
                        tf.makeList(newBoxes.toArray(new IStrategoTerm[newBoxes.size()])), boxes.getAnnotations())),
                    t.getAnnotations());
            }
        }



        return t;
    }


    private IStrategoTerm applyOffsideConstraintToSelector(IStrategoTerm t, IStrategoTerm posRef,
        IStrategoTerm termTarg) {

        if(t.equals(termTarg)) {
            // For all Z([], [b | bs]) in termTarg
            // for all boxes in bs with col <= than colRef, col = colRef + 1
            return applyOffsideConstraintToZBoxes(termTarg, posRef);
        }

        IStrategoTerm[] subTerms = new IStrategoTerm[t.getSubtermCount()];

        for(int i = 0; i < t.getSubtermCount(); i++) {
            subTerms[i] = applyOffsideConstraintToSelector(t.getSubterm(i), posRef, termTarg);
        }

        if(TermUtils.isAppl(t)) {
            return annotateTerm(tf.makeAppl(((IStrategoAppl) t).getConstructor(), subTerms), t.getAnnotations());
        } else if(TermUtils.isList(t)) {
            return tf.copyAttachments(t, tf.makeList(subTerms, t.getAnnotations()));
        }

        return t;
    }


    private IStrategoTerm applyOffsideConstraintToZBoxes(IStrategoTerm t, IStrategoTerm posRef) {
        if(TermUtils.isAppl(t)) {
            String constructorName = ((IStrategoAppl) t).getConstructor().getName();
            if(constructorName.equals("S")) {
                return t;
            }

            if(constructorName.equals("H") || constructorName.equals("V")) {
                List<IStrategoTerm> newBoxes = new ArrayList<>();
                IStrategoTerm boxes = t.getSubterm(1);
                for(IStrategoTerm subTerm : boxes) {
                    newBoxes.add(applyOffsideConstraintToZBoxes(subTerm, posRef));
                }
                return annotateTerm(
                    tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
                        tf.makeList(newBoxes.toArray(new IStrategoTerm[newBoxes.size()])), boxes.getAnnotations())),
                    t.getAnnotations());
            }

            if(constructorName.equals("Z")) {
                List<IStrategoTerm> newBoxes = new ArrayList<>();
                IStrategoTerm boxes = t.getSubterm(1);
                if(boxes.getSubtermCount() != 0) {
                    IStrategoTerm emptyBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                    newBoxes.add(applyOffsideConstraintToZBoxes(boxes.getSubterm(0), posRef));
                    for(int i = 1; i < boxes.getSubtermCount(); i++) {
                        IStrategoTerm pos = getPosition(boxes.getSubterm(i));

                        if(pos != null) {
                            int currCol = TermUtils.toJavaIntAt(pos, 1);
                            int refCol = TermUtils.toJavaIntAt(posRef, 1);
                            if(currCol <= refCol) {
                                // Make box H([SOpt(HS(), refcol - currCol + 1)], [S(""), boxes_i])
                                IStrategoTerm hopt =
                                    tf.makeAppl(tf.makeConstructor("SOpt", 2), tf.makeAppl(tf.makeConstructor("HS", 0)),
                                        tf.makeString("" + (refCol - currCol + 1)));

                                newBoxes.add(tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(hopt), tf
                                    .makeList(emptyBox, applyOffsideConstraintToZBoxes(boxes.getSubterm(i), posRef))));
                            } else {
                                newBoxes.add(applyOffsideConstraintToZBoxes(boxes.getSubterm(i), posRef));
                            }
                        } else {
                            newBoxes.add(applyOffsideConstraintToZBoxes(boxes.getSubterm(i), posRef));
                        }
                    }

                    return annotateTerm(
                        tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
                            tf.makeList(newBoxes.toArray(new IStrategoTerm[newBoxes.size()])), boxes.getAnnotations())),
                        t.getAnnotations());
                } else {
                    return t;
                }
            }

        }
//        logger.error("Not a box to apply offside constraint to!");
        return t;
    }

    private IStrategoTerm removeCurrentIndentation(IStrategoTerm t) {
        // if box is indentation box, i.e., H([SOpt(HS(), is)], [S(""), H([SOpt(HS(), "0")], <id>)])
        // get 'is' to discount it from shift
        String constructorName = ((IStrategoAppl) t).getConstructor().getName();

        // H([SOpt(HS(), is)], [S(""), H([SOpt(HS(), "0")], <id>)])
        if(constructorName.equals("H")) {
            // first list is not empty and second list has two elements
            if(t.getSubterm(0).getSubtermCount() != 0 && t.getSubterm(1).getSubtermCount() == 2) {
                // second list starts with S("")
                IStrategoTerm secondList = t.getSubterm(1).getSubterm(0);
                IStrategoTerm emptyStringBox = tf.makeAppl(tf.makeConstructor("S", 1), tf.makeString(""));
                if(secondList.equals(emptyStringBox)) {
                    // second element is H([SOpt(HS(), "0")], <id>)
                    IStrategoTerm hBox = t.getSubterm(1).getSubterm(1);
                    if(TermUtils.isAppl(hBox) && ((IStrategoAppl) hBox).getConstructor().getName().equals("H")) {
                        IStrategoTerm hBoxFirstList = hBox.getSubterm(0);
                        if(hBoxFirstList.getSubtermCount() == 1) {
                            IStrategoString zeroHS = tf.makeString("0");
                            if(hBoxFirstList.getSubterm(0).getSubterm(1).equals(zeroHS)) {
                                IStrategoTerm indentation = t.getSubterm(0).getSubterm(0).getSubterm(1);
                                assert TermUtils.isString(indentation);
                                IStrategoTerm noIndentConfig = tf.makeAppl(tf.makeConstructor("SOpt", 2),
                                    tf.makeAppl(tf.makeConstructor("HS", 0)), tf.makeString("0"));
                                return annotateTerm(tf.makeAppl(tf.makeConstructor("H", 2), tf.makeList(noIndentConfig),
                                    t.getSubterm(1)), t.getAnnotations());
                            }
                        }
                    }
                }
            }
        }

        // H(_, box*) box or V(_, box*) box or Z(_, box*) box
        if(constructorName.equals("H") || constructorName.equals("V") || constructorName.equals("Z")) {
            IStrategoTerm boxes = t.getSubterm(1);
            if(boxes.getSubtermCount() != 0) {
                List<IStrategoTerm> subterms = new ArrayList<>();

                subterms.add(removeCurrentIndentation(boxes.getSubterm(0)));
                for(int i = 1; i < boxes.getSubtermCount(); i++) {
                    subterms.add(boxes.getSubterm(i));
                }

                return annotateTerm(
                    tf.makeAppl(tf.makeConstructor(constructorName, 2), t.getSubterm(0), annotateTerm(
                        tf.makeList(subterms.toArray(new IStrategoTerm[subterms.size()])), boxes.getAnnotations())),
                    t.getAnnotations());
            }
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
                if(TermUtils.isAppl(anno) && (((IStrategoAppl) anno).getConstructor().getName().equals("Align")
                    || ((IStrategoAppl) anno).getConstructor().getName().equals("Offside")
                    || ((IStrategoAppl) anno).getConstructor().getName().equals("Indent"))) {
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


    private @Nullable IStrategoTerm getPosition(IStrategoTerm t) {
        if(t.getAnnotations() != null) {
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(TermUtils.isAppl(anno)
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                    return anno;
                }
            }
        }

        return null;
    }

    private IStrategoTerm getPositionWithLayout(IStrategoTerm t) {
        if(t.getAnnotations() != null) {
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(TermUtils.isAppl(anno)
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("PositionWithLayout")) {
                    return tf.makeAppl(tf.makeConstructor("Position", 2), anno.getSubterm(0), anno.getSubterm(1));
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
                if(TermUtils.isAppl(anno)
                    && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                    pos = anno;
                    if(foundRef) {
                        return anno;
                    }
                }
                if(TermUtils.isAppl(anno) && (((IStrategoAppl) anno).getConstructor().getName().equals("Align")
                    || ((IStrategoAppl) anno).getConstructor().getName().equals("Offside")
                    || ((IStrategoAppl) anno).getConstructor().getName().equals("Indent"))) {
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

    private IStrategoTerm updateColumnBoxes(IStrategoTerm t, IStrategoTerm position) {
        if(TermUtils.isAppl(t)) {
            // S(_)
            if(((IStrategoAppl) t).getConstructor().getName().equals("S")) {
                IStrategoString string = TermUtils.toStringAt(t, 0);
                if(!string.stringValue().equals("") || getPosition(t) != null) {
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
            List<IStrategoTerm> tail = new ArrayList<>();

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
                    assert TermUtils.isString(horizontalSpace);
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
                    IStrategoTerm verticalSpace = t.getSubterm(0).getSubterm(0).getSubterm(1);
                    assert TermUtils.isString(verticalSpace);
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

    private IStrategoTerm updateHorizontalListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position, int hs) {
        IStrategoTerm updatedPosition = shiftColumn(getEndPosition(head, position), hs);

        List<IStrategoTerm> newList = new ArrayList<>();

        newList.add(head);
        newList.addAll(updateColumnBoxesHorizontal(updatedPosition, hs, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        IStrategoTerm line = position.getSubterm(0);
        IStrategoTerm column = position.getSubterm(1);
        IStrategoTerm result;
        if(firstPositionBox != null) {
            result = annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            result = tf.annotateTerm(newTerm, t.getAnnotations());
        }

        return annotateBoxPositionWithLayout(result,
            tf.makeAppl(tf.makeConstructor("PositionWithLayout", 2), line, column), result.getAnnotations());
    }


    private IStrategoTerm updateVerticalListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position, int vs) {
        IStrategoTerm line = position.getSubterm(0);
        IStrategoTerm column = position.getSubterm(1);

        IStrategoTerm updatedPosition = shiftLine(getEndPosition(head, position), vs, column);

        List<IStrategoTerm> newList = new ArrayList<>();

        newList.add(head);
        newList.addAll(updateColumnBoxesVertical(updatedPosition, vs, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm result;
        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        if(firstPositionBox != null) {
            result = annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            result = tf.annotateTerm(newTerm, t.getAnnotations());
        }

        return annotateBoxPositionWithLayout(result,
            tf.makeAppl(tf.makeConstructor("PositionWithLayout", 2), line, column), result.getAnnotations());
    }


    private IStrategoTerm updateVerticalZListOfBoxes(IStrategoTerm t, IStrategoTerm head, List<IStrategoTerm> tail,
        IStrategoTerm position) {
        IStrategoTerm updatedPosition = shiftLine(getEndPosition(head, position), 1);

        List<IStrategoTerm> newList = new ArrayList<>();

        newList.add(head);
        newList.addAll(updateColumnBoxesVerticalZ(updatedPosition, 1, tail));

        IStrategoTerm newTerm = tf.makeAppl(((IStrategoAppl) t).getConstructor(), t.getSubterm(0), annotateTerm(
            tf.makeList(newList.toArray(new IStrategoTerm[newList.size()])), t.getSubterm(1).getAnnotations()));

        IStrategoTerm line = position.getSubterm(0);
        IStrategoTerm column = position.getSubterm(1);

        IStrategoTerm result;
        IStrategoTerm firstPositionBox = findFirstPosition(newTerm);
        if(firstPositionBox != null) {
            result = annotateBoxPosition(newTerm, firstPositionBox, t.getAnnotations());
        } else {
            result = tf.annotateTerm(newTerm, t.getAnnotations());
        }

        return annotateBoxPositionWithLayout(result,
            tf.makeAppl(tf.makeConstructor("PositionWithLayout", 2), line, column), result.getAnnotations());
    }


    private IStrategoTerm shiftColumn(IStrategoTerm position, int hs) {
        return tf.makeAppl(((IStrategoAppl) position).getConstructor(), position.getSubterm(0),
            tf.makeInt(TermUtils.toJavaIntAt (position, 1) + hs));
    }


    private IStrategoTerm shiftLine(IStrategoTerm position, int vs) {
        if(vs == 0) {
            return position;
        } else {
            return tf.makeAppl(tf.makeConstructor("Position", 2),
                tf.makeInt(TermUtils.toJavaIntAt(position, 0) + vs), tf.makeInt(1));
        }
    }

    private IStrategoTerm shiftLine(IStrategoTerm position, int vs, IStrategoTerm column) {
        return tf.makeAppl(tf.makeConstructor("Position", 2),
            tf.makeInt(TermUtils.toJavaIntAt(position, 0) + vs), column);
    }


    private IStrategoTerm getEndPosition(IStrategoTerm t, IStrategoTerm position) {
        if(TermUtils.isAppl(t)) {
            // S box
            if(((IStrategoAppl) t).getConstructor().getName().equals("S")) {
                IStrategoString text = TermUtils.toStringAt(t, 0);

                int column = TermUtils.toJavaIntAt(position, 1);

                return tf.makeAppl(tf.makeConstructor("Position", 2), position.getSubterm(0),
                    tf.makeInt(column + text.stringValue().length()));
            }
            // H box
            if(((IStrategoAppl) t).getConstructor().getName().equals("H")) {
                if(t.getSubterm(0).getSubtermCount() == 0) {
                    return getEndPositionHList(t.getSubterm(1), position, 1);
                } else {
                    IStrategoTerm horizontalSpace = t.getSubterm(0).getSubterm(0).getSubterm(1);
                    assert TermUtils.isString(horizontalSpace);

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
                    assert TermUtils.isString(verticalSpace);

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
        assert TermUtils.isList(t);

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
        assert TermUtils.isList(t);

        if(t.getSubtermCount() == 0) {
            return position;
        }

        if(t.getSubtermCount() == 1) {
            return getEndPosition(t.getSubterm(0), position);
        }

        IStrategoTerm column = position.getSubterm(1);
        IStrategoTerm endPosition = getEndPosition(TermUtils.toList(t).head(), position);

        return getEndPositionVList(TermUtils.toList(t).tail(), shiftLine(endPosition, vs, column), vs);

    }


    private IStrategoTerm getEndPositionZList(IStrategoTerm t, IStrategoTerm position) {
        assert TermUtils.isList(t);

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
        List<IStrategoTerm> newAnnotations = new ArrayList<>();

        for(IStrategoTerm anno : oldAnnotations) {
            if(TermUtils.isAppl(anno) && ((IStrategoAppl) anno).getConstructor().getName().equals("Position")) {
                continue;
            }
            newAnnotations.add(anno);
        }
        newAnnotations.add(position);
        return tf.annotateTerm(t, tf.makeList(newAnnotations.toArray(new IStrategoTerm[newAnnotations.size()])));
    }

    private IStrategoTerm annotateBoxPositionWithLayout(IStrategoTerm t, IStrategoTerm position,
        IStrategoList oldAnnotations) {
        List<IStrategoTerm> newAnnotations = new ArrayList<>();

        for(IStrategoTerm anno : oldAnnotations) {
            if(TermUtils.isAppl(anno)
                && ((IStrategoAppl) anno).getConstructor().getName().equals("PositionWithLayout")) {
                continue;
            }
            newAnnotations.add(anno);
        }
        newAnnotations.add(position);
        return tf.annotateTerm(t, tf.makeList(newAnnotations.toArray(new IStrategoTerm[newAnnotations.size()])));
    }


    private List<IStrategoTerm> updateColumnBoxesHorizontal(IStrategoTerm position, int hs, List<IStrategoTerm> list) {

        List<IStrategoTerm> result = new ArrayList<>();

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

        List<IStrategoTerm> result = new ArrayList<>();

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
        List<IStrategoTerm> result = new ArrayList<>();

        if(list.isEmpty()) {
            return result;
        }

        IStrategoTerm head = updateColumnBoxes(list.get(0), position);

        result.add(head);

        IStrategoTerm endPosition = shiftLine(getEndPosition(head, position), vs);

        result.addAll(updateColumnBoxesVertical(endPosition, vs, list.subList(1, list.size())));

        return result;
    }

    private IStrategoTerm annotateTerm(IStrategoTerm t, IStrategoList annotations) {
        return tf.annotateTerm(t, annotations);
    }

    private IStrategoTerm findFirstPosition(IStrategoTerm t) {
        if(t.getAnnotations() != null) {
            for(IStrategoTerm anno : t.getAnnotations()) {
                if(TermUtils.isAppl(anno)
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
