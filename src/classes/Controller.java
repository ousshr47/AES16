package classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.enzo.notification.Notification.Notifier;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.CryptoException;
import utils.CryptoUtils;
import utils.Hash;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class Controller implements Initializable
{
	final FileChooser fileChooser = new FileChooser();

	private File file = new File("");

	public static Stage stage;

	String abspath = "";

	public String nomFichier;

	String checksum = "";

	String symkey = "";

	File file2 = new File("");

	String regex = "([a-zA-Z]:)?(/[a-zA-Z0-9_.-]+)+/?";

	Pattern pattern = Pattern.compile(regex);

	@FXML
	private JFXButton join;

	@FXML
	private Label path;

	private String f0 = null;

	private String f1 = null;

	private String f2 = null;

	private ArrayList<String> f3;

	@FXML
	private JFXPasswordField key;

	@FXML
	private JFXTextField output;

	@FXML
	private JFXButton encrypt;

	@FXML
	private JFXButton decrypt;

	private void setExtFilters(FileChooser chooser) 
	{
		chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All files", "*.encrypted", "*.*"));
	}

	private static String toHex(byte[] bytes) 
	{
		return DatatypeConverter.printHexBinary(bytes);
	}

	@FXML
	void Upload(ActionEvent event) throws IOException, InterruptedException 
	{
		setExtFilters(fileChooser);
		file = fileChooser.showOpenDialog(stage);
		fileChooser.setTitle("Browse");
		if (file != null) 
		{
			abspath = file.getAbsolutePath();
			file2 = file;
			nomFichier = file.getName();
			System.out.println(nomFichier);
			path.setVisible(true);
			path.setText(file.getName());
		}
	}

	@FXML
	void Encrypt(ActionEvent event) throws Exception {
		if (nomFichier == null || nomFichier.length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No file selected !!");
		}
		if (key.getText() == null || key.getText().length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No key typed !!");
		}
		if (output.getText() == null || output.getText().length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No path selected !!");
		} else {
			String ext = (String) file.getName().subSequence(file.getName().indexOf("."), file.getName().length());
			System.out.println(ext);
			if (ext.equals(".encrypted")) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "File already encrypted !!");
			}
			boolean isMatched = Pattern.matches(regex, output.getText());
			if (!isMatched) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "Incorrect path format !!");
			}
			if (key.getText().length() != 16) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "Please type 16-Byte key !!");
			}
			if (!ext.equals(".encrypted") && key.getText().length() == 16 && isMatched) {
				File dir = new File(output.getText());
				if (!dir.exists()) {
					dir.mkdir();
				}
				String path2 = file.getAbsolutePath();
				File inputFile = new File(path2);
				String newname = file.getName().replaceFirst("[.][^.]+$", "");
				System.out.println(newname);
				File encryptedFile = new File(output.getText() + newname + ".encrypted");
				try {
					CryptoUtils.encrypt(key.getText(), inputFile, encryptedFile);

					symkey = key.getText();
					System.out.println(symkey);

					checksum = toHex(Hash.SHA512.checksum(encryptedFile));
					System.out.println(checksum);

					f0 = ext;
					f1 = checksum;
					f2 = symkey;

					File xmlfile = new File("src/xml/db.xml");
					String xmlpath = xmlfile.getCanonicalPath();
					System.out.println(xmlpath);

					saveToXML(xmlpath);

					Notifier.setOffsetX(825);
					Notifier.setOffsetY(400);
					Notifier.INSTANCE.notifySuccess("Success", "File encrypted !!");

					key.clear();
					output.clear();
					path.setVisible(false);
				} catch (CryptoException ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}

	public void saveToXML(String xml) {
		Document dom;
		Element e = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.newDocument();
			Element rootEle = dom.createElement("file");
			e = dom.createElement("f0");
			e.appendChild(dom.createTextNode(f0));
			rootEle.appendChild(e);
			e = dom.createElement("f1");
			e.appendChild(dom.createTextNode(f1));
			rootEle.appendChild(e);
			e = dom.createElement("f2");
			e.appendChild(dom.createTextNode(f2));
			rootEle.appendChild(e);
			dom.appendChild(rootEle);

			try {
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "db.dtd");
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
				tr.transform(new DOMSource(dom), new StreamResult(new FileOutputStream(xml)));
			} catch (TransformerException te) {
				System.out.println(te.getMessage());
			}
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		} catch (ParserConfigurationException pce) {
			System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
		}
	}

	public boolean readXML(String xml) {
		f3 = new ArrayList<String>();
		Document dom;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(xml);
			Element doc = dom.getDocumentElement();
			f0 = getTextValue(f0, doc, "f0");
			if (f0 != null) {
				if (!f0.isEmpty())
					f3.add(f0);
			}
			f1 = getTextValue(f1, doc, "f1");
			if (f1 != null) {
				if (!f1.isEmpty())
					f3.add(f1);
			}
			f2 = getTextValue(f2, doc, "f2");
			if (f2 != null) {
				if (!f2.isEmpty())
					f3.add(f2);
			}
			return true;
		} catch (ParserConfigurationException pce) {
			System.out.println(pce.getMessage());
		} catch (SAXException se) {
			System.out.println(se.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
		return false;
	}

	private String getTextValue(String def, Element doc, String tag) {
		String value = def;
		NodeList nl;
		nl = doc.getElementsByTagName(tag);
		if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
			value = nl.item(0).getFirstChild().getNodeValue();
		}
		return value;
	}

	@FXML
	void Decrypt(ActionEvent event) throws Exception {
		if (nomFichier == null || nomFichier.length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No file selected !!");
		}
		if (key.getText() == null || key.getText().length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No key typed !!");
		}
		if (output.getText() == null || output.getText().length() == 0) {
			Notifier.setOffsetX(825);
			Notifier.setOffsetY(400);
			Notifier.INSTANCE.notifyError("Error", "No path selected !!");
		} else {
			String ext = (String) file.getName().subSequence(file.getName().indexOf("."), file.getName().length());
			System.out.println(ext);
			if (!ext.equals(".encrypted")) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "File already decrypted !!");
			}
			boolean isMatched = Pattern.matches(regex, output.getText());
			if (!isMatched) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "Incorrect path format !!");
			}
			if (key.getText().length() != 16) {
				Notifier.setOffsetX(825);
				Notifier.setOffsetY(400);
				Notifier.INSTANCE.notifyError("Error", "Please type 16-Byte key !!");
			}
			String path2 = file.getAbsolutePath();
			File inputFile = new File(path2);
			String eqhash = toHex(Hash.SHA512.checksum(inputFile));

			System.out.println(eqhash);

			File xmlfile = new File("src/xml/db.xml");
			String xmlpath = xmlfile.getCanonicalPath();
			System.out.println(xmlpath);

			readXML(xmlpath);
			System.out.println(readXML(xmlpath));

			for (String string : f3) {
				System.out.println(string);
				f0 = f3.get(0);
				f1 = f3.get(1);
				f2 = f3.get(2);
			}

			System.out.println(f0 + " " + f1 + " " + f2);

			if (ext.equals(".encrypted") && isMatched && key.getText().length() == 16) {
				File dir = new File(output.getText());
				if (!dir.exists()) {
					dir.mkdir();
				}
				String newname = file.getName().replaceFirst("[.][^.]+$", "");
				System.out.println(newname);
				File decryptedFile = new File(output.getText() + newname + f0);
				try {
					CryptoUtils.decrypt(key.getText(), inputFile, decryptedFile);

					Notifier.setOffsetX(825);
					Notifier.setOffsetY(400);
					Notifier.INSTANCE.notifySuccess("Success", "File decrypted !!");

					key.clear();
					output.clear();
					path.setVisible(false);
					xmlfile.delete();
				} catch (CryptoException ex) {
					Notifier.setOffsetX(825);
					Notifier.setOffsetY(400);
					Notifier.INSTANCE.notifyError("Error", "Incorrect key !!");

					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) 
	{
		path.setVisible(false);

	}

}
