package cn.tm.ms.restful.entity;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

//book 实体类
@XmlRootElement
public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlAttribute(name = "bookId")
	public Long getBookId() {
		return 111l;
	}

	@XmlAttribute(name = "bookName")
	public String getBookName() {
		return "bookName";
	}

	@XmlAttribute(name = "publisher")
	public String getPublisher() {
		return "publisher";
	}
}