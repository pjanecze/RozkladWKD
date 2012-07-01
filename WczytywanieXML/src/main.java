import java.io.BufferedOutputStream;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class main {

	private static String[][] tablicawagr;
	private static String[][] tablicagrwa;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		tablicawagr = getRozklad("Rozklad_wa_gr.xml");
		

		saveToXml(tablicawagr, "rozkladwa-gr_new.xml");

		
		test("rozkladwa-gr_new.xml");
		
		

	}

	private static void test(String string) {
		int size = 68;
		String[] table = new String[size];
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(string));
			
			NodeList dane = doc.getElementsByTagName("dane");
			
			for(int i=0; i<size;i++){
				table[i]= dane.item(i).getFirstChild().getNodeValue();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<size;i++){
			if(table[i].trim().startsWith("D")){
				
				String[] podzielone = table[i].split(" ");
				for(int j=0; j<podzielone.length;j++){
					System.out.print(podzielone[j] + ", ");
				}
				System.out.println("\nDLUGOSC: "+podzielone.length+"!!!!");
			} else {
				
				String[] podzielone = table[i].split(" ");
				for(int j=0; j<podzielone.length;j++){
					System.out.print(podzielone[j] + ", ");
				}
				System.out.println("\nDLUGOSC: "+podzielone.length+"!!!!");
			}
		}
		
	}

	public static String[][] getRozklad(String url) {
		try {

			String[][] tablica = new String[60][34];
			String[][] tablicaPoprawna = new String[68][30];
			Integer sizeSec = 0;
			Integer sizeFirst;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(url));
			sizeFirst = doc.getElementsByTagName("Row").getLength();

			Element data, cellItem, cell;
			NodeList row;
			int cellLength = 37;
			long czas;
			czas = System.currentTimeMillis();

			Element docEle = doc.getDocumentElement();
			row = docEle.getElementsByTagName("Row");
			for (int i = 0; i < sizeFirst; i++) {

				cell = (Element) row.item(i);
				for (int j = 3; j < cellLength; j++) {

					cellItem = (Element) cell.getElementsByTagName("Cell")
							.item(j);
					data = (Element) cellItem.getElementsByTagName("Data")
							.item(0);
					if(data != null && data.getFirstChild()==null){
						tablica[i][j-3]= "";
					}
					else if(data != null){
						
							tablica[i][j-3]= data.getFirstChild().getNodeValue().trim();
						
					}
					
				}

			}

			// size first = 60, sizesec = 34 : next first = 30, next sec = 68
			for (int i = 0; i < sizeFirst; i++) {

				for (int j = 0; j < cellLength-3; j++) {
					if (i < 30) {
						tablicaPoprawna[j][i] = 
							tablica[i][j];
						
					} else {
						tablicaPoprawna[j + 34][i - 30] = tablica[i][j];
						
					}

				}
			}
			for (int i = 0; i < 68; i++) {
				//System.out.println("\nNowy");
				for (int j = 0; j <30; j++) {
					//System.out.println(tablicaPoprawna[i][j]);
				}
			}
			return tablicaPoprawna;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public static void saveToXml(String[][] tablica, String name) {
		/*try {
			// tworzymy fabryke, która posluzy do „produkcji” obiektu klasy
			// DocumentBuilder
			DocumentBuilderFactory fabryka = DocumentBuilderFactory
					.newInstance();
			// tworzymy obiekt parsera wykorzystujac do tego utworzona wczesniej
			// fabryke
			DocumentBuilder parser = fabryka.newDocumentBuilder();
			// tworzymy pusty dokument DOM
			Document dokument = parser.newDocument();

			Element root = dokument.createElement("root");
			Element kolejka = dokument.createElement("kolejka");
			
			dokument.appendChild(root);
			root.appendChild(kolejka);
			String tekst;
			for (int i = 0; i < 68; i++) {
				kolejka.
				tekst = null;
				for (int j = 0; j < 30; j++) {
					tekst += tablica[i][j] + " ";
				}
				Text elemText = dokument.createTextNode(tekst);
				kolejka.appendChild(elemText);

			}
			
			XMLSerializer serializer = new XMLSerializer();
			serializer.setOutputCharStream(new java.io.FileWriter(
					new File(name)));
			serializer.serialize(dokument);

		} catch (Exception exp) {
			System.out.println(exp.getMessage());
		}*/
		
		Tekst[] tekst = new Tekst[68];
		
		String tekstA;
		for(int i=0;i<68; i++){
			tekstA = "";
			for (int j = 0; j < 30; j++) {
				if(tablica[i][j]==null){
					tekstA+="";
					
					}
				else {
					tablica[i][j] = tablica[i][j].replaceAll(">", "");
					tablica[i][j] = tablica[i][j].replaceAll("|", "");
					System.out.print(tablica[i][j]);
					tekstA += tablica[i][j] + " ";
				}
				
			}System.out.println("");
			tekst[i]= new Tekst(tekstA);
			
		}
		nu.xom.Element root = new nu.xom.Element("table");
		for(int i=0;i<68;i++){
			root.appendChild(tekst[i].getXML());
		}
		nu.xom.Document doc = new nu.xom.Document(root);
		try {
			Tekst.format(System.out, doc);
			Tekst.format(new BufferedOutputStream(new FileOutputStream(name)), doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
