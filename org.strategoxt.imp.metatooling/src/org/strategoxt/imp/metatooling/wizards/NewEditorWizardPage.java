package org.strategoxt.imp.metatooling.wizards;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (esv).
 */

public class NewEditorWizardPage extends WizardPage {
	
	private Text inputProjectName;
	
	private Text inputLanguageName;
	
	private Text inputPackageName;
	
	private Text inputExtensions;
	
	private boolean isInputProjectNameChanged;
	
	private boolean isInputPackageNameChanged;
	
	private boolean isInputExtensionsChanged;
	
	private boolean ignoreEvents;

	/**
	 * Constructor for SampleNewWizardPage.
	 */
	public NewEditorWizardPage() {
		super("wizardPage");
		setTitle("Spoofax Editor Project");
		setDescription("This wizard creates a new Spoofax editor project.");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		
		/*
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		*/
				
		new Label(container, SWT.NULL).setText("&Project name:");
		inputProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputProjectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputProjectName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					distributeProjectName();
					onChange();
				}
			}
		});
				
		new Label(container, SWT.NULL).setText("&Language name:");
		inputLanguageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputLanguageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputLanguageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					distributeLanguageName();
					isInputProjectNameChanged = true;
					onChange();
				}
			}
		});
		
		new Label(container, SWT.NULL).setText("&Plugin ID and package name:");
		inputPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputPackageName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputPackageName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					isInputPackageNameChanged = true;
					onChange();
				}
			}
		});
				
		new Label(container, SWT.NULL).setText("&File extensions:");
		inputExtensions = new Text(container, SWT.BORDER | SWT.SINGLE);
		inputExtensions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		inputExtensions.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (!ignoreEvents) {
					isInputExtensionsChanged = true;
					onChange();
				}
			}
		});

		setControl(container);
		setPageComplete(false);
		inputProjectName.setFocus();
	}

	private void distributeProjectName() {
		if (!isInputProjectNameChanged || getInputLanguageName().length() == 0
				|| getInputLanguageName().equals(toLanguageName(getInputProjectName()))) {
			ignoreEvents = true;
			inputLanguageName.setText(toLanguageName(getInputProjectName()));
			isInputProjectNameChanged = false;
			ignoreEvents = false;
			distributeLanguageName();
		}
		if (!isInputPackageNameChanged || getInputPackageName().length() == 0
				|| getInputPackageName().equals(toPackageName(getInputProjectName()))) {
			ignoreEvents = true;
			inputPackageName.setText(toPackageName(getInputProjectName()));
			isInputPackageNameChanged = false;
			ignoreEvents = false;
		}
	}

	private void distributeLanguageName() {
		if (!isInputExtensionsChanged || getInputExtensions().length() == 0
				|| getInputExtensions().equals(toExtension(getInputLanguageName()))) {
			ignoreEvents = true;
			inputExtensions.setText(toExtension(getInputLanguageName()));
			isInputExtensionsChanged = false;
			ignoreEvents = false;
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */
	private void onChange() {
		setErrorMessage(null);
		
		if (getInputProjectName().length() == 0) {
			setErrorStatus("Project name must be specified");
			return;
		}
		if (getInputLanguageName().length() == 0) {
			setErrorStatus("Language name must be specified");
			return;
		}	
		
		if (!isValidProjectName(getInputProjectName())) {
			setErrorStatus("Project name must be valid");
			return;
		}
		if (!toLanguageName(getInputLanguageName()).equalsIgnoreCase(getInputLanguageName())) {
			setErrorStatus("Language name must be valid");
			return;
		}

		if (getInputPackageName().length() == 0) {
			setErrorStatus("Package name must be specified");
			return;
		}
		if (!getInputPackageName().equalsIgnoreCase(toPackageName(getInputPackageName()))
				|| getInputPackageName().indexOf("..") != -1
				|| getInputPackageName().endsWith(".")) {
			setErrorStatus("Package name must be valid");
			return;
		}

		if (getInputExtensions().length() == 0) {
			setErrorStatus("File extension must be specified");
			return;
		}
		if (getInputExtensions().indexOf(".") != -1 || getInputExtensions().replace('\\', '/').indexOf("/") != -1
				|| getInputExtensions().indexOf(":") > -1) {
			setErrorStatus("File extension must be valid");
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.getRoot().getProject(getInputProjectName()).exists()) {
			setErrorStatus("A project with this name already exists");
			return;
		}
	

		if (getInputProjectName().indexOf(' ') != -1) {
			setWarningStatus("Project names with spaces may not be supported depending on your configuration");
		} else {
			setErrorStatus(null);
		}
	}

	private static boolean isValidProjectName(String name) {
		for (char c : name.toCharArray()) {
			if (!(Character.isLetterOrDigit(c) || c == '_' || c == ' ' || c == '-' || c == '.'
				|| c == '(' || c == ')' || c == '#' || c == '+' || c =='[' || c == ']' || c == '@'))
				return false;
		}
		return true;
	}

	private static String toLanguageName(String name) {
		char[] input = name.replace(' ', '-').toCharArray();
		StringBuilder output = new StringBuilder();
		int i = 0;
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetter(c) || c == '-' || c == '_') {
				output.append(c);
				break;
			}
		}
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetterOrDigit(c) || c == '-' || c == '_')
				output.append(c);
		}
		if (output.length() > 0)
			output.setCharAt(0, Character.toUpperCase(output.charAt(0))); // SDF wants a capital here
		return output.toString();
	}
	
	private static String toPackageName(String name) {
		char[] input = name.replace(' ', '-').toCharArray();
		StringBuilder output = new StringBuilder();
		int i = 0;
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetter(c) || c == '.' || c == '_') {
				output.append(c);
				break;
			}
		}
		while (i < input.length) {
			char c = input[i++];
			if (Character.isLetterOrDigit(c) || c == '.' || c == '_')
				output.append(c);
		}
		String result = output.toString().replaceAll("\\.(?=\\.|[0-9]|\\Z)", "");
		return result;
	}
	
	private static String toExtension(String name) {
		String input = name.toLowerCase().replace("-", "").replace(".", "").replace(" ", "").replace(":", "");
		String prefix = input.substring(0, Math.min(input.length(), 3));
		if (input.length() == 0) return "";
		
		for (int i = input.length() - 1;; i--) {
			if (!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
				return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
			} else if (i == prefix.length()) {
				return prefix + input.substring(i);
			}
		}
	}

	private void setErrorStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private void setWarningStatus(String message) {
		if (getErrorMessage() == null)
			setErrorMessage(message);
	}
	
	public String getInputProjectName() {
		return inputProjectName.getText().trim();
	}
	
	public String getInputLanguageName() {
		return inputLanguageName.getText().trim();
	}
	
	public String getInputPackageName() {
		return inputPackageName.getText().trim();
	}
	
	public String getInputExtensions() {
		return inputExtensions.getText().trim();
	}
}
