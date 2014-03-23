package cz.juzna.intellij.kdyby.events;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpRecursiveElementVisitor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class EventsUtil {


	public static Collection<EventListener> findListeners(final Field field) {
		final Collection<EventListener> result = new ArrayList<EventListener>();
		final PhpClass phpClass = field.getContainingClass();

		PhpIndex phpIndex = PhpIndex.getInstance(field.getProject());
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
									if (listener == null || !(listener.getEvent() instanceof NetteEvent)) {
										continue;
									}
									NetteEvent event = (NetteEvent) listener.getEvent();
									if (phpClass.getPresentableFQN().equals(event.getClassName())
											&& (field.getName().equals(event.getEventName()))) {

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
				String callbackMethodName = null;

				PsiElement _tempEl = element;
				while (!(_tempEl instanceof PhpClass) && _tempEl.getParent() != null) {
					_tempEl = _tempEl.getParent();
				}
				PhpClass phpClass;
				if (_tempEl instanceof PhpClass) {
					phpClass = (PhpClass) _tempEl;
				} else {
					return null;
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
						return null;
					}

				} else {
					fullEventName = ElementValueResolver.resolve((PhpPsiElement) element.getFirstChild());
					if (fullEventName == null) {
						return null;
					}
				}
				Event event = createEvent(fullEventName);
				if (event == null) {
					return null;
				}
				if (methods.size() == 0) {
					methods.add(event instanceof NetteEvent ? ((NetteEvent) event).getEventName() : event.getIdentifier());
				}
				Collection<EventListener> listeners = new ArrayList<EventListener>();
				for (String method : methods) {
					Method cb = phpClass.findMethodByName(method);
					if (cb != null) {
						listeners.add(new EventListener(event, cb));
					}
				}
				return listeners;
			}
		});
	}

	public static boolean isInGetSubscribedEvents(PsiElement el) {

		while (el != null) {
			if (el instanceof Method) {
				if (((Method) el).getName().equals("getSubscribedEvents")) {
					return true;
				}
			}

			el = el.getParent();
		}

		return false;
	}

	@Nullable
	public static Event createEvent(String value) {
		if (!value.contains("::")) {
			return new Event(value);
		}
		String[] tmp = value.split("::");
		if (tmp.length != 2) {
			return null;
		}
		String className = tmp[0].replace("\\\\", "\\"), fieldName = tmp[1];
		if (className.startsWith("\\")) {
			className = className.substring(1);
		}
		return new NetteEvent(className, fieldName);
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
