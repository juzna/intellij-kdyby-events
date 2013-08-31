package cz.juzna.intellij.kdyby.events;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;

import java.util.ArrayList;
import java.util.List;


public class EventsUtil {
	/**
	 * Najde vsechny handlery (metody), ktere se pripojuji na event (property)
	 *
	 * Napr. pro Pd\Order\OrderService::$onStatusChanged najde metody BackOfficeUpdater::updateOrderStatus()
	 *
	 * @param project
	 * @param forField
	 * @return
	 */
	public static List<Method> findEventHandlers(Project project, final Field forField) {
		final List<Method> result = new ArrayList<Method>();

		// najdu tridy implementujici subscriber
		PhpIndex phpIndex = PhpIndex.getInstance(project);
		for (PhpClass clazz : phpIndex.getAllSubclasses("Kdyby\\Events\\Subscriber")) {

			final Method method = clazz.findMethodByName("getSubscribedEvents");
			if (method == null) continue;

			// find all elements of returned array
			method.acceptChildren(new PhpRecursiveElementVisitor() {
				@Override
				public void visitPhpReturn(PhpReturn r) {
					if (r.getArgument() instanceof ArrayCreationExpression) {
						ArrayCreationExpression arr = (ArrayCreationExpression) r.getArgument();

						assert arr != null;
						for (PsiElement el_ : arr.getChildren()) {
							String fullEventName;
							String callbackMethodName;

							if (el_ instanceof ArrayHashElement) { // key => value
								ArrayHashElement el;
								el = (ArrayHashElement) el_;
								if ( ! (el.getKey() instanceof StringLiteralExpression)) {
									// invalid key
									continue;
								}
								if ( ! (el.getValue() instanceof StringLiteralExpression)) {
									// invalid value
									continue;
								}

								fullEventName = ((StringLiteralExpression) el.getKey()).getContents();
								callbackMethodName = ((StringLiteralExpression) el.getValue()).getContents();

							} else {
								if ( ! (el_.getFirstChild() instanceof StringLiteralExpression)) {
									// invalid value
									continue;
								}

								fullEventName = ((StringLiteralExpression) el_.getFirstChild()).getContents();
								callbackMethodName = fullEventName.split("::")[1];
							}

							String[] tmp = fullEventName.split("::");
							if (tmp.length != 2) {
								// invalid event name
								continue;
							}

							String eventNameClass, eventNameField;
							eventNameClass = tmp[0];
							eventNameField = tmp[1];

							// match?
							if (forField.getContainingClass().getPresentableFQN().equals(eventNameClass) && forField.getName().equals(eventNameField)) {
								Method cb = method.getContainingClass().findMethodByName(callbackMethodName);
								if (cb != null) {
									result.add(cb);
								}
							}
						}

					}

					super.visitPhpReturn(r);
				}
			});
		}

		return result.size() > 0 ? result : null;
	}
}
