package cz.juzna.intellij.kdyby.events;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class EventsUtil {


	public static Collection<EventListener> findListeners(final Event event, Project project) {
		final Collection<EventListener> result = new ArrayList<EventListener>();

		PhpIndex phpIndex = PhpIndex.getInstance(project);
		for (final PhpClass clazz : phpIndex.getAllSubclasses("Kdyby\\Events\\Subscriber")) {

			final Method method = clazz.findMethodByName("getSubscribedEvents");
			if (method == null) continue;

			// find all elements of returned array
			try {
				method.acceptChildren(new PhpRecursiveElementVisitor() {
					@Override
					public void visitPhpReturn(PhpReturn r) {
						if (r.getArgument() instanceof ArrayCreationExpression) {
							ArrayCreationExpression arr = (ArrayCreationExpression) r.getArgument();

							assert arr != null;
							for (PsiElement el_ : arr.getChildren()) {
								for (EventListener listener : resolveListeners(el_)) {
									if (listener.getEvent().equals(event)) {
										result.add(listener);
									}
								}
							}
						}

						super.visitPhpReturn(r);
					}
				});
			} catch (Exception e) {
				System.out.println("Error parsing class " + clazz.getPresentableFQN());
				e.printStackTrace();
			}
		}

		return result;

	}

	@Nullable
	private static Collection<EventListener> resolveListeners(final PsiElement element) {
		if (!(element.getParent() instanceof ArrayCreationExpression)) {
			throw new IllegalArgumentException();
		}

		return ApplicationManager.getApplication().runReadAction(new Computable<Collection<EventListener>>() {
			@Override
			public Collection<EventListener> compute() {
				String fullEventName;

				PsiElement _tempEl = element;
				while (!(_tempEl instanceof PhpClass) && _tempEl.getParent() != null) {
					_tempEl = _tempEl.getParent();
				}
				PhpClass phpClass;
				if (_tempEl instanceof PhpClass) {
					phpClass = (PhpClass) _tempEl;
				} else {
					return new ArrayList<EventListener>();
				}
				Collection<String> methods = new ArrayList<String>();

				if (element instanceof ArrayHashElement) { // key => value
					ArrayHashElement el;
					el = (ArrayHashElement) element;

					fullEventName = ElementValueResolver.resolve((el.getKey()));
					if (el.getValue() instanceof ArrayCreationExpression) {
						PsiElement[] children = el.getValue().getChildren();
						// [[method, priority], ...]
						if (children.length > 0 && children[0].getFirstChild() instanceof ArrayCreationExpression) {
							for (PsiElement listenerEl : children) {
								if (listenerEl.getFirstChild() instanceof ArrayCreationExpression
										&& listenerEl.getFirstChild().getChildren().length == 2) {
									methods.add(ElementValueResolver.resolve(listenerEl.getFirstChild().getChildren()[0].getFirstChild()));
								}
							}
						} else if (children.length == 2) { // [method, priority]
							methods.add(ElementValueResolver.resolve(children[0].getFirstChild()));
						}
					} else {
						methods.add(ElementValueResolver.resolve(el.getValue()));
					}
					if (fullEventName == null || methods.size() == 0) {
						return new ArrayList<EventListener>();
					}

				} else {
					fullEventName = ElementValueResolver.resolve(element.getFirstChild());
					if (fullEventName == null) {
						return new ArrayList<EventListener>();
					}
				}
				Event event = EventFactory.create(fullEventName);
				if (event == null) {
					return new ArrayList<EventListener>();
				}
				if (methods.size() == 0) {
					methods.add(event.getShortName());
				}
				Collection<EventListener> listeners = new ArrayList<EventListener>();
				for (String method : methods) {
					Method cb = phpClass.findMethodByName(method);
					listeners.add(new EventListener(event, cb));
				}
				return listeners;
			}
		});
	}

	public static boolean isInGetSubscribedEvents(PsiElement el) {
		Method method = getContainingMethod(el);
		return method != null && method.getName().equals("getSubscribedEvents");
	}

	public static Method getContainingMethod(PsiElement el) {
		while (el != null) {
			if (el instanceof Method) {
				return (Method) el;
			}
			el = el.getParent();
		}

		return null;
	}

	public static PsiElement getEventIdentifierInArray(PsiElement element) {
		while (true) {
			if (element == null ||
					!(element instanceof PhpPsiElement)
					|| !isInGetSubscribedEvents(element)) {
				return null;
			}
			if (element.getParent() instanceof ArrayHashElement && ((ArrayHashElement) element.getParent()).getKey() != element.getFirstChild()) {
				return null;
			}
			if (element.getParent() instanceof ArrayCreationExpression) {
				break;
			}
			element = element.getParent();
		}


		if (element instanceof ArrayHashElement) {
			element = ((ArrayHashElement) element).getKey();
		} else {
			element = element.getFirstChild();
		}

		return element;
	}
}
