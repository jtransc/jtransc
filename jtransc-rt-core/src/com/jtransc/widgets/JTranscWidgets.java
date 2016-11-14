package com.jtransc.widgets;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.target.Js;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"WeakerAccess", "unused"})
public class JTranscWidgets {
	static public JTranscWidgets impl = createDefault();

	static public JTranscWidgets createDefault() {
		if (JTranscSystem.isJsBrowser()) {
			return createDefaultJs();
		} else {
			return new JTranscWidgets();
		}
	}

	static public JTranscWidgets createDefaultJs() {
		return new JTranscWidgets() {
			@Override
			public void setTimeout(int delay, Runnable step) {
				Js.v_raw("setTimeout(function() { p1['{% METHOD java.lang.Runnable:run %}'](); }, p0);");
			}

			@Override
			public Widget createComponent(String kind) {
				return new Widget(lastId++, kind) {
					@Override
					protected void init() {
						switch (kind) {
							case "button":
								Js.v_raw("this.element = document.createElement('button');");
								break;
							case "image":
								Js.v_raw("this.element = document.createElement('canvas');");
								break;
							default:
								Js.v_raw("this.element = document.createElement('div');");
								break;
						}
						Js.v_raw("this.element.id = 'j' + this['{% FIELD com.jtransc.widgets.JTranscWidgets$Widget:id %}'];");
						if (Objects.equals(kind, "frame")) {
							Js.v_raw("this.element.style.background = 'gray';");
							Js.v_raw("document.body.appendChild(this.element);");
						}
					}

					@Override
					@JTranscMethodBody(target = "js", value = {
						"var that = this;",
						"this.element.onclick = function() { that['{% METHOD com.jtransc.widgets.JTranscWidgets$Widget:dispatchEvent %}'](N.str('click')); };",
					})
					public void watchMouseEvents() {
					}

					@Override
					public void setParent(Widget parent) {
						Js.v_raw("p0.element.appendChild(this.element);");
					}

					@Override
					public void setText(String text) {
						Js.v_raw("this.element.innerText = N.istr(p0);");
					}

					@Override
					public void setBounds(int x, int y, int width, int height) {
						Js.v_raw("this.element.style.left = '' + p0 + 'px';");
						Js.v_raw("this.element.style.top = '' + p1 + 'px';");
						Js.v_raw("this.element.style.width = '' + p2 + 'px';");
						Js.v_raw("this.element.style.height = '' + p3 + 'px';");
					}

					@Override
					public void setPixels(int[] rgba, int width, int height) {
						Js.v_raw("var canvas = this.element;");
						Js.v_raw("var width = p1;");
						Js.v_raw("var height = p2;");
						Js.v_raw("var size = width * height;");
						Js.v_raw("canvas.width = width;");
						Js.v_raw("canvas.height = height;");
						Js.v_raw("canvas.style.width = '' + width + 'px';");
						Js.v_raw("canvas.style.height = '' + height + 'px';");

						Js.v_raw("var ctx = canvas.getContext('2d');");
						Js.v_raw("var imageData = ctx.createImageData(width, height);");
						Js.v_raw("for (var n = 0, m = 0; n < size; n++) { var rgba = p0.data[n]; imageData.data[m++] = (rgba >> 16) & 0xFF; imageData.data[m++] = (rgba >> 8) & 0xFF; imageData.data[m++] = (rgba >> 0) & 0xFF; imageData.data[m++] = (rgba >> 24) & 0xFF; }");
						Js.v_raw("ctx.putImageData(imageData, 0, 0);");

						//System.out.println("setPixels!!" + width + "," + height);
					}

					@Override
					public void setVisible(boolean visible) {
						Js.v_raw("this.element.style.visibility = p0 ? 'visible' : 'hidden';");
					}

					@Override
					public void setEnabled(boolean enabled) {
						Js.v_raw("this.element.disable = !p0;");
					}
				};
			}
		};
	}

	protected int lastId = 0;

	public Widget createComponent(String kind) {
		return new Widget(lastId++, kind);
	}

	public void setTimeout(int delay, Runnable step) {
		System.out.println("Not implemented setTimeout:" + delay);
	}

	static public interface EventListener {
		public void handle(String kind);
	}

	static public class Widget {
		public int id;
		public String kind;
		public EventListener listener;

		public Widget(int id, String kind) {
			this.id = id;
			this.kind = kind;
			init();
		}

		protected void init() {
			System.out.println(this + ".init()");
		}

		public void watchMouseEvents() {
		}

		public void dispatchEvent(String kind) {
			if (listener != null) {
				listener.handle(kind);
			}
		}

		public void setParent(Widget parent) {
			System.out.println(this + ".setParent(" + parent + ")");
		}

		public void setText(String text) {
			System.out.println(this + ".setText('" + text + "')");
		}

		public void setPixels(int[] rgba, int width, int height) {
			System.out.println(this + ".setPixels(" + width + ", " + height + ")");
		}

		public void setBounds(int x, int y, int width, int height) {
			System.out.println(this + ".setBounds(" + x + ", " + y + ", " + width + ", " + height + ")");
		}

		public void setVisible(boolean visible) {
			System.out.println(this + ".setVisible(" + visible + ")");
		}

		public void setEnabled(boolean enabled) {
			System.out.println(this + ".setEnabled(" + enabled + ")");
		}

		@Override
		public String toString() {
			return "JTranscWidgets.Component(" + id + ":" + kind + ")";
		}
	}
}
