import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


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
	public static void main(String[] args)  {

        try {
            tablicawagr = getRozkladCSV("gr_wa.csv");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        //saveToSQL(tablicawagr, "rozkladwa-gr_new.xml");

//        tablicagrwa = getRozkladCSV("Rozklad_gr_wa.txt");


       // saveToSQL(tablicagrwa, "rozkladgr_wa_new.txt");

		
		//test("rozkladwa-gr_new.xml");
		
		

	}

    private static String[][] getRozkladCSV(String s) throws IOException {
        //use buffering, reading one line at a time
        //FileReader always assumes default encoding is OK!
        BufferedReader input =  new BufferedReader(new FileReader(s));
        try {
            String line = null; //not declared within while loop
            /*
            * readLine is a bit quirky :
            * it returns the content of a line MINUS the newline.
            * it returns null only for the END of the stream.
            * it returns an empty String if two newlines appear in a row.
            */

            HashMap<String, ArrayList<String>> rows = new HashMap<String, ArrayList<String>>();

            while (( line = input.readLine()) != null){
                String[] rowarr = line.split(";");
                ArrayList<String> rowlist;
                if(rowarr.length >1) {
                    System.out.println("size: " + rowarr.length);

                    String key = rowarr[1];
                    if(!rows.containsKey(key)) {
                        rowlist = new ArrayList<String>();
                    } else {
                        rowlist = rows.get(key);
                    }
                    for(int i = 3; i < rowarr.length; i++) {
                       rowlist.add(key);
                    }
                    rows.put(key, rowlist);
                }
            }

            ArrayList<ArrayList<String>> finalList = new ArrayList<ArrayList<String>>(rows.size());
            int i = 0;
            Collection<ArrayList<String>> valuesrows = rows.values();
            for(ArrayList<String> valrow : valuesrows) {
                i = 0;
                for(String val : valrow) {
                    ArrayList<String> connection;
                    if(finalList.size() <=i)
                        connection = new ArrayList<String>(28);
                    else {
                        connection = finalList.get(i);
                        finalList.remove(i);
                    }

                    connection.add(val);


                    finalList.add(i, connection);
                    i++;
                }

            }

            for(ArrayList<String> connection : finalList) {
                System.out.println("length: " + connection.size());
            }

        } finally {
            input.close();
        }
        return  null;
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


			String[][] tablicaPoprawna = new String[68][31];
			Integer sizeSec = 0;
			Integer sizeFirst;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new File(url));
			sizeFirst = doc.getElementsByTagName("Row").getLength();

            String[][] tablica = new String[sizeFirst][34];

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

                        try{
							tablica[i][j-3]= data.getFirstChild().getNodeValue().trim();
                        } catch (Exception e) {
                                 e.printStackTrace();
                        }
						
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
			// tworzymy fabryke, ktora posluzy do produkcji obiektu klasy
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
