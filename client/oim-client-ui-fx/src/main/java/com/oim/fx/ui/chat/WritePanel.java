package com.oim.fx.ui.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import com.oim.common.util.EmojiUtil;
import com.oim.fx.common.DataConverter;
import com.sun.javafx.webkit.Accessor;
import com.sun.javafx.webkit.InputMethodClientImpl;
import com.sun.javafx.webkit.KeyCodeMap;
import com.sun.webkit.Utilities;
import com.sun.webkit.WebPage;
import com.sun.webkit.dom.HTMLBodyElementImpl;
import com.sun.webkit.event.WCInputMethodEvent;
import com.sun.webkit.event.WCKeyEvent;
import com.sun.webkit.event.WCMouseEvent;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodHighlight;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class WritePanel extends VBox {
	static {
		Utilities.setUtilities(new UtilitiesImpl());
	}
	private String fontName = "Microsoft YaHei";
	private int fontSize = 12;
	private Color color = Color.BLACK;
	private boolean bold = false;
	private boolean underline = false;
	private boolean italic = false;

	WebView webView = new WebView();
	WebPage webPage;
	WebEngine webEngine;
	private DataConverter<String, String> emojiConverter;

	public WritePanel() {
		initComponent();
		iniEvent();
	}

	private void initComponent() {
		this.getChildren().add(webView);
		webEngine = webView.getEngine();
		webPage = Accessor.getPageFor(webEngine);
		webPage.setEditable(true);
		webView.setFocusTraversable(true);
		// webView.getEngine().setUserStyleSheetLocation(getClass().getResource("/resources/common/css/webview.css").toExternalForm());
		// webPage.setBackgroundColor(255);
		webView.setPrefWidth(300);
		// webView.setOpacity(0.92);
		// webView.setBlendMode(BlendMode.DARKEN);
		// .setBackground(Background.EMPTY);
		webView.setOnDragDropped(a -> {

		});

		webEngine.getLoadWorker().stateProperty().addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {

			if (newState == State.SUCCEEDED) {
				initWeb(webEngine);
			}
		});
		initializeHtml();
	}

	private void iniEvent() {
		webView.setOnInputMethodTextChanged(new EventHandler<InputMethodEvent>() {

			@Override
			public void handle(InputMethodEvent e) {
				processInputMethodEvent(e);
			}
		});

		// webView.setOnKeyTyped(new EventHandler<KeyEvent>() {
		//
		// @Override
		// public void handle(KeyEvent event) {
		// //event.consume();
		// // System.out.println(event);
		// }
		// });

		// webView.removeEventHandler(KeyEvent.ANY, null);
		webView.addEventHandler(KeyEvent.ANY,
				event -> {
					// processKeyEvent(event);
				});

		// webView.addEventHandler(KeyEvent.ANY,
		// event -> {
		// // System.out.println(event);
		// // processKeyEvent(event);
		// });

		// webView.setOnKeyReleased(v -> {
		// if (v.isControlDown() && v.getCode() == KeyCode.V) {
		// // Clipboard clipboard = Clipboard.getSystemClipboard();
		//
		// // if(clipboard.hasHtml()){
		// // String html=clipboard.getHtml();
		// // System.out.println(clipboard.getString());
		// // String t=OnlyStringUtil.html(html);
		// // final ClipboardContent content = new ClipboardContent();
		// // content.putString(t);
		// // clipboard.setContent(content);
		// // System.out.println(clipboard.getString());
		// // }
		// v.consume();
		// //System.out.println(v);
		// }
		//
		// });

		// webView.setOnContextMenuRequested(e -> {
		// // System.out.println(e);
		// });
	}

	public WebView getWebView() {
		return webView;
	}

	@SuppressWarnings("unchecked")
	public KeyEvent createKeyEvent(
			EventType<? extends KeyEvent> eventType,
			KeyCode code, String character, String text,
			boolean shiftDown, boolean controlDown,
			boolean altDown, boolean metaDown) {
		return new KeyEvent((EventType<KeyEvent>) eventType, character, text, code, shiftDown, controlDown, altDown, metaDown);
	}

	private void processInputMethodEvent(InputMethodEvent ie) {
		if (webPage == null) {
			return;
		}
		String text = ie.getCommitted();
		String emoji = null;
		// System.out.println(text);
		boolean isEmoji = EmojiUtil.isEmoji(text);
		if (isEmoji && null != emojiConverter) {
			// TODO
			emoji = emojiConverter.converter(text);
//			if (null != emoji && !"".equals(emoji)) {
//				this.insertSelectionEmojiHtml(text);
//				// text = OnlyStringUtil.html(text);
//				// KeyEvent ke = createKeyEvent(KeyEvent.KEY_PRESSED,
//				// KeyCode.CONTROL, "", "", false, true, false, false);
//				// processKeyEvent(ke);
//				// Clipboard clipboard = Clipboard.getSystemClipboard();
//				// String html=null;
//				// if (clipboard.hasHtml()) {
//				// html= clipboard.getHtml();
//				// }
//				//
//				// ClipboardContent content = new ClipboardContent();
//				// content.putHtml(text);
//				// clipboard.setContent(content);
//				// KeyEvent ke = createKeyEvent(KeyEvent.KEY_PRESSED, KeyCode.V,
//				// "", "", false, true, false, false);
//				// processKeyEvent(ke);
//				// ke = createKeyEvent(KeyEvent.KEY_RELEASED, KeyCode.V, "", "",
//				// false, true, false, false);
//				// processKeyEvent(ke);
//			}
			ie.consume();
		}

		if (null != emoji && !"".equals(emoji)) {
			this.insertSelectionEmojiHtml(emoji);
		} else {
			InputMethodEvent e = new InputMethodEvent(
					ie.getSource(),
					ie.getTarget(),
					ie.getEventType(),
					ie.getComposed(),
					text,
					ie.getCaretPosition());
			WCInputMethodEvent imEvent = InputMethodClientImpl.convertToWCInputMethodEvent(e);
			// WCInputMethodEvent imEvent = convertToWCInputMethodEvent(e);
			if (webPage.dispatchInputMethodEvent(imEvent)) {
				ie.consume();
				return;
			}
		}
	}

	public static WCInputMethodEvent convertToWCInputMethodEvent(InputMethodEvent ie) {
		List<Integer> underlines = new ArrayList<Integer>();
		StringBuilder composed = new StringBuilder();
		int pos = 0;

		// Scan the given composedText to find input method highlight attribute
		// runs.
		for (InputMethodTextRun run : ie.getComposed()) {
			String rawText = run.getText();

			// Convert highlight information of the attribute run into a
			// CompositionUnderline.
			InputMethodHighlight imh = run.getHighlight();
			underlines.add(pos);
			underlines.add(pos + rawText.length());
			// WebKit CompostionUnderline supports only two kinds of
			// highlighting
			// attributes, thin and thick underlines. The SELECTED_CONVERTED
			// and SELECTED_RAW attributes of JavaFX are mapped to the thick
			// one.
			underlines.add((imh == InputMethodHighlight.SELECTED_CONVERTED ||
					imh == InputMethodHighlight.SELECTED_RAW) ? 1 : 0);
			pos += rawText.length();
			composed.append(rawText);
		}

		int size = underlines.size();
		// In case there's no highlight information, create an underline element
		// for the entire text
		if (size == 0) {
			underlines.add(0);
			underlines.add(pos);
			underlines.add(0); // thin underline
			size = underlines.size();
		}
		int[] attributes = new int[size];
		for (int i = 0; i < size; i++) {
			attributes[i] = underlines.get(i);
		}
		return new WCInputMethodEvent(ie.getCommitted(), composed.toString(), attributes, ie.getCaretPosition());
	}

	public void processKeyEvent(KeyEvent ev) {
		if (webPage == null)
			return;
		ev.consume();
		String text = null;
		String keyIdentifier = null;
		int windowsVirtualKeyCode = 0;
		if (ev.getEventType() == KeyEvent.KEY_TYPED) {
			text = ev.getCharacter();
		} else {
			System.out.println(ev.getCode());
			KeyCodeMap.Entry keyCodeEntry = KeyCodeMap.lookup(ev.getCode());
			keyIdentifier = keyCodeEntry.getKeyIdentifier();
			windowsVirtualKeyCode = keyCodeEntry.getWindowsVirtualKeyCode();
		}

		WCKeyEvent keyEvent = new WCKeyEvent(
				idMap.get(ev.getEventType()),
				text,
				keyIdentifier,
				windowsVirtualKeyCode,
				ev.isShiftDown(), ev.isControlDown(),
				ev.isAltDown(), ev.isMetaDown(), System.currentTimeMillis());
		if (webPage.dispatchKeyEvent(keyEvent)) {
			ev.consume();
		}
	}

	public void setWebOnDragDropped(EventHandler<? super DragEvent> value) {
		webView.setOnDragDropped(value);
	}

	private void updateStyle() {
		Document doc = webPage.getDocument(webPage.getMainFrame());
		if (doc instanceof HTMLDocument) {
			HTMLDocument htmlDocument = (HTMLDocument) doc;
			HTMLElement htmlDocumentElement = (HTMLElement) htmlDocument.getDocumentElement();
			HTMLElement htmlBodyElement = (HTMLElement) htmlDocumentElement.getElementsByTagName("body").item(0);
			htmlBodyElement.setAttribute("style", createStyleValue().toString());
		}
	}

	private StringBuilder createStyle() {
		StringBuilder style = new StringBuilder();
		style.append("style=\"");
		style.append(createStyleValue());
		style.append("\"");
		return style;
	}

	private StringBuilder createStyleValue() {
		StringBuilder style = new StringBuilder();

		// style.append("background-color:rgba(0,152,50,0.7);");
		style.append("word-wrap:break-word;");// word-wrap: break-word;
		style.append("font-family:").append(fontName).append(";");
		style.append("font-size:").append(fontSize).append("px;");
		if (underline) {
			style.append("margin-top:0;text-decoration:underline;");
		} else {
			style.append("margin-top:0;");
		}
		if (italic) {
			style.append("font-style:italic;");
		}
		// if (italic) {
		// style.append("font-style:oblique;");
		// }
		if (bold) {
			style.append("font-weight:bold;");
		}
		if (null != color) {
			String c = colorValueToHex(color);
			style.append("color:#");
			style.append(c);
			style.append(";");
		}
		return style;
	}

	private String colorValueToHex(Color c) {
		return String.format((Locale) null, "%02x%02x%02x", Math.round(c.getRed() * 255),
				Math.round(c.getGreen() * 255), Math.round(c.getBlue() * 255));
	}

	public void initializeHtml() {
		initializeHtml(true);
	}

	public void initializeHtml(boolean createStyle) {
		StringBuilder html = new StringBuilder();

		html.append("<!doctype html>");
		html.append("<html>");
		html.append("	<head>");
		html.append("		<meta charset=\"utf-8\">");
		html.append("		<title></title>");
		html.append("	</head>");
		html.append("	<body contenteditable=\"true\" ").append((createStyle ? createStyle() : "")).append("></body>");
		html.append("</html>");
		webPage.load(webPage.getMainFrame(), html.toString(), "text/html");
		// webEngine.load(this.getClass().getResource("/resources/chat/html/write_2.html").toString());
	}

	public String getHtml() {
		String html = webPage.getHtml(webPage.getMainFrame());
		// Document doc = webPage.getDocument(webPage.getMainFrame());
		// html=doc.toString();
		return html;
	}

	public void insert(String text) {
		Document doc = webPage.getDocument(webPage.getMainFrame());
		if (doc instanceof HTMLDocument) {
			HTMLDocument htmlDocument = (HTMLDocument) doc;
			HTMLElement htmlDocumentElement = (HTMLElement) htmlDocument.getDocumentElement();
			HTMLElement htmlBodyElement = (HTMLElement) htmlDocumentElement.getElementsByTagName("body").item(0);

			if (htmlBodyElement instanceof HTMLBodyElementImpl) {
				// HTMLBodyElementImpl htmlBodyElementImpl =
				// (HTMLBodyElementImpl) htmlBodyElement;
				// Element e = htmlBodyElementImpl.getOffsetParent();
				// htmlBodyElementImpl.insertBefore(newChild, refChild)
				// htmlDocument.
			}

			Element e = htmlDocument.createElement("div");
			e.setTextContent(text);

			int positionOffset = webPage.getClientInsertPositionOffset();
			int index = (positionOffset + 1);
			NodeList nodeList = htmlBodyElement.getChildNodes();
			int length = nodeList.getLength();
			if (index < 0) {
				htmlBodyElement.appendChild(e);
			} else if (index < length) {
				org.w3c.dom.Node node = nodeList.item(index);
				// htmlBodyElement.
				htmlBodyElement.insertBefore(e, node);
			} else {
				htmlBodyElement.appendChild(e);
			}

			webPage.getClientInsertPositionOffset();

			// webView.set
			// System.out.println(htmlDocument.);

			//
			// org.w3c.dom.Node n = htmlBodyElement.getLastChild();
			//
			//
			//
			// webPage.getClientInsertPositionOffset();
			// // htmlBodyElement.ge
			// // hd.appendChild(e);
			// htmlDocument.getp
			//
		}
	}

	public void insertImage(String path, String id, String name, String value) {
		Document doc = webPage.getDocument(webPage.getMainFrame());
		if (doc instanceof HTMLDocument) {
			HTMLDocument htmlDocument = (HTMLDocument) doc;
			HTMLElement htmlDocumentElement = (HTMLElement) htmlDocument.getDocumentElement();
			HTMLElement htmlBodyElement = (HTMLElement) htmlDocumentElement.getElementsByTagName("body").item(0);

			if (htmlBodyElement instanceof HTMLBodyElementImpl) {
				// HTMLBodyElementImpl htmlBodyElementImpl =
				// (HTMLBodyElementImpl) htmlBodyElement;
				// Element e = htmlBodyElementImpl.getOffsetParent();
				// htmlBodyElementImpl.insertBefore(newChild, refChild)
				// htmlDocument.
			}

			Element e = htmlDocument.createElement("img");
			e.setAttribute("id", id);
			e.setAttribute("name", name);
			e.setAttribute("value", value);
			e.setAttribute("src", path);

			// webEngine.get
			// webView.get

			int positionOffset = webPage.getClientInsertPositionOffset();
			int index = (positionOffset + 1);
			NodeList nodeList = htmlBodyElement.getChildNodes();
			int length = nodeList.getLength();
			if (index < 0) {
				htmlBodyElement.appendChild(e);
			} else if (index < length) {
				org.w3c.dom.Node node = nodeList.item(index);
				htmlBodyElement.insertBefore(e, node);
			} else {
				htmlBodyElement.appendChild(e);
			}
			webPage.getClientInsertPositionOffset();
		}
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
		updateStyle();
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
		updateStyle();
	}

	public Color getColor() {
		return color;
	}

	public String getWebColor() {
		return colorValueToHex(color);
	}

	public void setColor(Color color) {
		this.color = color;
		updateStyle();
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
		updateStyle();
	}

	public boolean isUnderline() {
		return underline;
	}

	public void setUnderline(boolean underline) {
		this.underline = underline;
		updateStyle();
	}

	public boolean isItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
		updateStyle();
	}

	public void insertSelectionHtml(String html) {
		// String js=getJavaScript(escapeJavaStyleString(html, true, true));
		String js = createSelectionJavaScript(html);
		webEngine.executeScript(js);
	}

	public void insertSelectionEmojiHtml(String html) {
		String js = createSelectionJavaScriptEmoji(html);
		webEngine.executeScript(js);
	}

	public void insertLastHtml(String html) {
		// String js=getJavaScript(escapeJavaStyleString(html, true, true));
		String js = createLastJavaScript(html);
		webEngine.executeScript(js);
	}

	public void clearBody() {
		String js = createClearBodyJavaScript();
		webEngine.executeScript(js);
	}

	private String createSelectionJavaScriptEmoji(String html) {
		StringBuilder js = new StringBuilder();
		js.append("function insertHtmlAtCursor(html) {\n");
		js.append("    var range, node;\n");
		js.append("    var hasSelection = window.getSelection;\n");
		js.append("    var selection = window.getSelection();\n");
		js.append("    if (hasSelection &&selection.rangeCount>0&& selection.getRangeAt)  {\n");
		js.append("        range = window.getSelection().getRangeAt(0);\n");
		// js.append(" node = range.createContextualFragment(html);\n");
		// js.append(" range.insertNode(node);\n");

		js.append("		   var element = document.createElement(\"p\");\n");
		js.append("		   element.innerHTML = html;\n");
		js.append("		   var nodes = element.childNodes;");
		js.append("		   var l = nodes.length;");
		js.append("		   for (var i = 0; i < l; i++) {");
		js.append("		       node = nodes[i];");
		js.append("		       range.insertNode(node);");
		js.append("		   }");
		js.append("        if(node){\n");

		// js.append(" range = range.cloneRange()\n");
		js.append("			   range.setStartAfter(node);\n");

		// js.append(" selection.focusNode(node);// range.selectNode(node); ");

		// js.append(" if (range.select) { ");
		// js.append(" range.select(); ");
		// js.append(" } ");

		// 有用
		// js.append(" range.collapse(true);\n");
		// js.append(" selection.removeAllRanges();\n");
		// js.append(" selection.addRange(range);\n");

		// js.append(" window.oim.newRangeEnd();\n");

		// js.append(" window.focus();\n");

		js.append("        }\n");
		// js.append(" range.setStartAfter(node);");
		js.append("    } else if (document.selection && document.selection.createRange) {\n");
		js.append("        document.selection.createRange().pasteHTML(html);\n");
		js.append("    } else {\n");
		js.append("        var element = document.createElement('div');\n");
		js.append("        element.innerHTML = html;");
		js.append("        node = element.childNodes[0];");
		js.append("        document.body.appendChild(node);\n");
		js.append("    }");
		js.append("}");
		js.append("insertHtmlAtCursor('");
		js.append(html);
		js.append("');");
		String jsInsertHtml = js.toString();
		return jsInsertHtml;
	}

	private String createSelectionJavaScript(String html) {
		StringBuilder js = new StringBuilder();
		js.append("function insertHtmlAtCursor(html) {\n");
		js.append("    var range, node;\n");
		js.append("    var hasSelection = window.getSelection;\n");
		js.append("    var selection = window.getSelection();\n");
		js.append("    if (hasSelection &&selection.rangeCount>0&& selection.getRangeAt)  {\n");
		js.append("        range = window.getSelection().getRangeAt(0);\n");
		// js.append(" node = range.createContextualFragment(html);\n");
		// js.append(" range.insertNode(node);\n");

		js.append("		   var element = document.createElement(\"p\");\n");
		js.append("		   element.innerHTML = html;\n");
		js.append("		   var nodes = element.childNodes;");
		js.append("		   var l = nodes.length;");
		js.append("		   for (var i = 0; i < l; i++) {");
		js.append("		       node = nodes[i];");
		js.append("		       range.insertNode(node);");
		js.append("		   }");
		js.append("        if(node){\n");

		// js.append(" range = range.cloneRange()\n");
		js.append("			   range.setStartAfter(node);\n");

		// js.append(" selection.focusNode(node);// range.selectNode(node); ");

		// js.append(" if (range.select) { ");
		// js.append(" range.select(); ");
		// js.append(" } ");

		// 有用
		js.append(" range.collapse(true);\n");
		js.append(" selection.removeAllRanges();\n");
		js.append(" selection.addRange(range);\n");

		// js.append(" window.oim.newRangeEnd();\n");

		// js.append(" window.focus();\n");

		js.append("        }\n");
		// js.append(" range.setStartAfter(node);");
		js.append("    } else if (document.selection && document.selection.createRange) {\n");
		js.append("        document.selection.createRange().pasteHTML(html);\n");
		js.append("    } else {\n");
		js.append("        var element = document.createElement('div');\n");
		js.append("        element.innerHTML = html;");
		js.append("        node = element.childNodes[0];");
		js.append("        document.body.appendChild(node);\n");
		js.append("    }");
		js.append("}");
		js.append("insertHtmlAtCursor('");
		js.append(html);
		js.append("');");
		String jsInsertHtml = js.toString();
		return jsInsertHtml;
	}

	private String createLastJavaScript(String html) {
		StringBuilder js = new StringBuilder();
		js.append("function insertHtmlAtCursor(html) {\n");
		js.append("    var range, node;\n");
		js.append("    var element = document.createElement('div');\n");
		js.append("    element.innerHTML = html;");
		js.append("    node = element.childNodes[0];");
		js.append("    document.body.appendChild(node);\n");
		js.append("}");
		js.append("insertHtmlAtCursor('");
		js.append(html);
		js.append("');");
		String jsInsertHtml = js.toString();
		return jsInsertHtml;
	}

	private String createClearBodyJavaScript() {
		StringBuilder js = new StringBuilder();
		js.append("function clearBody(){\n");
		js.append("    document.body.innerHTML=\"\";\n");
		js.append("}");
		js.append("clearBody();");
		String jsInsertHtml = js.toString();
		return jsInsertHtml;
	}

	private String hex(int i) {
		return Integer.toHexString(i);
	}

	public String escapeJavaStyleString(String str, boolean escapeSingleQuote, boolean escapeForwardSlash) {
		StringBuilder out = new StringBuilder("");
		if (str == null) {
			return null;
		}
		int sz;
		sz = str.length();
		for (int i = 0; i < sz; i++) {
			char ch = str.charAt(i);

			// handle unicode
			if (ch > 0xfff) {
				out.append("\\u").append(hex(ch));
			} else if (ch > 0xff) {
				out.append("\\u0").append(hex(ch));
			} else if (ch > 0x7f) {
				out.append("\\u00").append(hex(ch));
			} else if (ch < 32) {
				switch (ch) {
				case '\b':
					out.append('\\');
					out.append('b');
					break;
				case '\n':
					out.append('\\');
					out.append('n');
					break;
				case '\t':
					out.append('\\');
					out.append('t');
					break;
				case '\f':
					out.append('\\');
					out.append('f');
					break;
				case '\r':
					out.append('\\');
					out.append('r');
					break;
				default:
					if (ch > 0xf) {
						out.append("\\u00").append(hex(ch));
					} else {
						out.append("\\u000").append(hex(ch));
					}
					break;
				}
			} else {
				switch (ch) {
				case '\'':
					if (escapeSingleQuote) {
						out.append('\\');
					}
					out.append('\'');
					break;
				case '"':
					out.append('\\');
					out.append('"');
					break;
				case '\\':
					out.append('\\');
					out.append('\\');
					break;
				case '/':
					if (escapeForwardSlash) {
						out.append('\\');
					}
					out.append('/');
					break;
				default:
					out.append(ch);
					break;
				}
			}
		}
		return out.toString();
	}

	static final Map<Object, Integer> idMap = new HashMap<Object, Integer>();

	// private void processKeyEvent(KeyEvent ev) {
	// if (webPage == null)
	// return;
	//
	// String text = null;
	// String keyIdentifier = null;
	// int windowsVirtualKeyCode = 0;
	// if (ev.getEventType() == KeyEvent.KEY_TYPED) {
	// text = ev.getCharacter();
	// } else {
	// KeyCodeMap.Entry keyCodeEntry = KeyCodeMap.lookup(ev.getCode());
	// keyIdentifier = keyCodeEntry.getKeyIdentifier();
	// windowsVirtualKeyCode = keyCodeEntry.getWindowsVirtualKeyCode();
	// }
	//
	// WCKeyEvent keyEvent = new WCKeyEvent(
	// idMap.get(ev.getEventType()),
	// text,
	// keyIdentifier,
	// windowsVirtualKeyCode,
	// ev.isShiftDown(), ev.isControlDown(),
	// ev.isAltDown(), ev.isMetaDown(), System.currentTimeMillis());
	// if (webPage.dispatchKeyEvent(keyEvent)) {
	// ev.consume();
	// }
	// }
	//
	static {
		idMap.put(MouseButton.NONE, WCMouseEvent.NOBUTTON);
		idMap.put(MouseButton.PRIMARY, WCMouseEvent.BUTTON1);
		idMap.put(MouseButton.MIDDLE, WCMouseEvent.BUTTON2);
		idMap.put(MouseButton.SECONDARY, WCMouseEvent.BUTTON3);

		idMap.put(MouseEvent.MOUSE_PRESSED, WCMouseEvent.MOUSE_PRESSED);
		idMap.put(MouseEvent.MOUSE_RELEASED, WCMouseEvent.MOUSE_RELEASED);
		idMap.put(MouseEvent.MOUSE_MOVED, WCMouseEvent.MOUSE_MOVED);
		idMap.put(MouseEvent.MOUSE_DRAGGED, WCMouseEvent.MOUSE_DRAGGED);

		idMap.put(KeyEvent.KEY_PRESSED, WCKeyEvent.KEY_PRESSED);
		idMap.put(KeyEvent.KEY_RELEASED, WCKeyEvent.KEY_RELEASED);
		idMap.put(KeyEvent.KEY_TYPED, WCKeyEvent.KEY_TYPED);
	}

	private void initWeb(WebEngine webEngine) {
		JSObject window = (JSObject) webEngine.executeScript("window");
		window.setMember("oim", new JavaApplication());
	}

	public class JavaApplication {

		public void newRangeEnd() {
			// webPage.dispatchFocusEvent(fe);
		}
	}

	public void setEmojiConverter(DataConverter<String, String> emojiConverter) {
		this.emojiConverter = emojiConverter;
	}
}
