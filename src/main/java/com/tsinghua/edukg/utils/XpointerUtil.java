package com.tsinghua.edukg.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XpointerUtil {

	private static Pattern pattern = Pattern.compile("(/[A-Z0-9]+(\\[\\d+\\])*)+/text\\(\\)\\[\\d+\\],'',\\d+");
	private static Pattern elePattern = Pattern.compile("([A-Za-z0-9()]+)(\\[((\\d+))\\])*");
	private static String startTag = "#tag-start#";
	private static String endTag = "#tag-end#";

	public static String parse(String html, String url){
		String result = null;
		if(null != html){
			Document doc = Jsoup.parse(html);
			Element element = null;
			String text = null;
			Xpoint[] xpoints = parseXpoint(url);
			if(null != xpoints){
				if(xpoints[0].equals(xpoints[1])){
					element = digElement(doc, xpoints[0]);
					result = element.textNodes().get(xpoints[0].getTextIndex()).text().substring(xpoints[0].pointerIndex, xpoints[1].pointerIndex);
				}else{
					element = digElement(doc, xpoints[0]);
					text = element.textNodes().get(xpoints[0].getTextIndex()).text();
					text = text.substring(0, xpoints[0].pointerIndex) + startTag + text.substring(xpoints[0].pointerIndex, text.length());
					element.textNodes().get(xpoints[0].getTextIndex()).text(text);
					element = digElement(doc, xpoints[1]);
					text = element.textNodes().get(xpoints[1].getTextIndex()).text();
					text = text.substring(0, xpoints[1].pointerIndex) + endTag + text.substring(xpoints[1].pointerIndex, text.length());
					element.textNodes().get(xpoints[1].getTextIndex()).text(text);
					String docstr = doc.toString();
					result = docstr.substring(docstr.indexOf(startTag) + startTag.length(), docstr.indexOf(endTag)).replaceAll("\\s+", "").replaceAll("<[^>]+>", "");
				}
			}
		}
		return result;
	}

	public static List<String> getPager(String html, String url){
		Document doc = Jsoup.parse(html);
		List<String> pagerList = new ArrayList<String>();
		Element element = null;
		String text = null;
		Xpoint[] xpoints = parseXpoint(url);
		int bias = 0;
		if(null != xpoints){
			if(xpoints[0].equals(xpoints[1])){
				bias = startTag.length();
			}
			// 前标识
			element = digElement(doc, xpoints[0]);
			text = element.textNodes().get(xpoints[0].getTextIndex()).text();
			text = text.substring(0, xpoints[0].pointerIndex) + startTag + text.substring(xpoints[0].pointerIndex, text.length());
			element.textNodes().get(xpoints[0].getTextIndex()).text(text);
			// 后标识
			element = digElement(doc, xpoints[1]);
			text = element.textNodes().get(xpoints[1].getTextIndex()).text();
			text = text.substring(0, xpoints[1].pointerIndex + bias) + endTag + text.substring(xpoints[1].pointerIndex + bias, text.length());
			element.textNodes().get(xpoints[1].getTextIndex()).text(text);
			// 找pager
			String docstr = doc.toString();
			pagerList.add(getLastPager(docstr.substring(0, docstr.indexOf(startTag))));
			if(pagerList.get(0) == null){
				return pagerList;
			}else{
				pagerList.addAll(getPager(docstr.substring(docstr.indexOf(startTag) + startTag.length(), docstr.indexOf(endTag))));
			}
		}
		return pagerList;
	}

	private static String getLastPager(String str){
		Pattern pattern = Pattern.compile("(page\\d+)");
		String pagerStr = null;
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			pagerStr = matcher.group(1);
		}
		return pagerStr;
	}

	private static List<String> getPager(String str){
		List<String> pager = new ArrayList<String>();
		Pattern pattern = Pattern.compile("(page\\d+)");
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()){
			pager.add(matcher.group(1));
		}
		return pager;
	}

	private static Element digElement(Document doc, Xpoint xpoint){
		List<Ele> pathEle = xpoint.getPathEle();
		Element element = null;
		for(int i = 0; i < pathEle.size(); i++){
			if(null == element){
				element = doc.select(pathEle.get(i).getNodeName()).get(pathEle.get(i).getIndex());
			}else{
				element = element.select(pathEle.get(i).getNodeName()).get(pathEle.get(i).getIndex());
			}
		}
		return element;
	}

	private static Xpoint[] parseXpoint(String url){
		List<Xpoint> xpoints = new ArrayList<Xpoint>();
		Matcher matcher = pattern.matcher(url);
		while(matcher.find()){
			xpoints.add(parseSubXpoint(matcher.group(0)));
		}
		if(xpoints.size() == 2){
			return xpoints.toArray(new Xpoint[] {});
		}else{
			return null;
		}
	}

	private static Xpoint parseSubXpoint(String range){
		String[] s = range.split(",");
		String[] e = s[0].split("/");
		List<Ele> eles = new ArrayList<Ele>();
		Integer textIndex = null;
		for(int i = 0; i < e.length; i++){
			Matcher matcher = elePattern.matcher(e[i]);
			if(matcher.find()){
				if(!e[i].contains("text")){
					eles.add(new Ele(matcher.group(1).toLowerCase(), matcher.group(3)));
				}else{
					textIndex = Integer.parseInt(matcher.group(3)) - 1;
				}
			}
		}
		return new Xpoint(eles, textIndex, Integer.parseInt(s[2]));
	}

	private static class Ele{

		private String nodeName;
		private Integer index;

		public Ele(String nodeName, String index){
			super();
			this.nodeName = nodeName;
			if(StringUtils.isEmpty(index)){
				this.index = 0;
			}else{
				this.index = Integer.parseInt(index) - 1;
			}
		}

		public String getNodeName(){
			return this.nodeName;
		}

		public Integer getIndex(){
			return this.index;
		}

		@Override
		public String toString(){
			return "Ele [nodeName=" + this.nodeName + ", index=" + this.index + "]";
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.index == null) ? 0 : this.index.hashCode());
			result = prime * result + ((this.nodeName == null) ? 0 : this.nodeName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj){
				return true;
			}
			if(obj == null){
				return false;
			}
			if(this.getClass() != obj.getClass()){
				return false;
			}
			Ele other = (Ele) obj;
			if(this.index == null){
				if(other.index != null){
					return false;
				}
			}else if(!this.index.equals(other.index)){
				return false;
			}
			if(this.nodeName == null){
				if(other.nodeName != null){
					return false;
				}
			}else if(!this.nodeName.equals(other.nodeName)){
				return false;
			}
			return true;
		}
	}

	private static class Xpoint{

		private List<Ele> pathEle;
		private Integer textIndex;
		private Integer pointerIndex;

		public Xpoint(List<Ele> pathEle, Integer textIndex, Integer pointerIndex){
			super();
			this.pathEle = pathEle;
			this.textIndex = textIndex;
			this.pointerIndex = pointerIndex;
		}

		public List<Ele> getPathEle(){
			return this.pathEle;
		}

		public Integer getTextIndex(){
			return this.textIndex;
		}

		@Override
		public String toString(){
			return "Xpoint [pathEle=" + this.pathEle + ", textIndex=" + this.textIndex + ", pointerIndex=" + this.pointerIndex + "]";
		}

		@Override
		public int hashCode(){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.pathEle == null) ? 0 : this.pathEle.hashCode());
			result = prime * result + ((this.textIndex == null) ? 0 : this.textIndex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if(this == obj){
				return true;
			}
			if(obj == null){
				return false;
			}
			if(this.getClass() != obj.getClass()){
				return false;
			}
			Xpoint other = (Xpoint) obj;
			if(this.pathEle == null){
				if(other.pathEle != null){
					return false;
				}
			}else if(!this.pathEle.equals(other.pathEle)){
				return false;
			}
			if(this.textIndex == null){
				if(other.textIndex != null){
					return false;
				}
			}else if(!this.textIndex.equals(other.textIndex)){
				return false;
			}
			return true;
		}
	}
}
