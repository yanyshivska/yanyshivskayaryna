package consoleYanyshivska;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FileInfo {
	private String sender;
	private String filename;
	private String content;

	public FileInfo() {
	}
	
	public FileInfo(String sender, File file) throws IOException{
		if(!file.exists() || file.isDirectory())
			throw new IOException("The specified file "+file.getCanonicalPath()+ " does not exist or is not a file!");
		
		this.sender = sender;
		this.filename = file.getName();
		Encoder encoder = Base64.getEncoder();
		this.content = encoder.encodeToString(Files.readAllBytes(file.toPath()));
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public byte[] getContentBytes(){
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(this.content);
	}
}