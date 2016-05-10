package consoleYanyshivska;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WrappedList{
	
	public List<String> items;
	
	public WrappedList(){}
	
	public WrappedList(List<String> items){
		this.items = items;
	}
}