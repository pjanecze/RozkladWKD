import java.io.OutputStream;

import nu.xom.*;

public class Tekst {
	private String tekst;

	public Tekst(String tekst) {
		this.tekst = tekst;
	}
	public Element getXML(){
		Element dane = new Element("dane");
		dane.appendChild(tekst);
		return dane;
	}
	public Tekst(Element dane){
		tekst = dane.getValue();
	}
	public String toString(){return tekst;}
	
	public static void format(OutputStream os,Document doc) throws Exception {
		Serializer serializer = new Serializer(os, "Windows-1250");
		serializer.setIndent(4);
		serializer.setMaxLength(200);
		serializer.write(doc);
		serializer.flush();
	}
}
