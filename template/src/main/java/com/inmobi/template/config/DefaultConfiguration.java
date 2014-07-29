package com.inmobi.template.config;

import lombok.Getter;
import lombok.Setter;

import org.apache.velocity.tools.generic.MathTool;

import com.google.inject.Inject;
import com.inmobi.template.formatter.TemplateDecorator;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.template.interfaces.TemplateConfiguration;
import com.inmobi.template.interfaces.Tools;

public class DefaultConfiguration implements TemplateConfiguration{
	
	@Getter
	@Setter
	@Inject
	private GsonManager gsonManager;
	
	@Getter
	@Setter
	@Inject
	private Tools		tool;
	
	@Getter
	@Setter
	@Inject
	private MathTool    mathTool;
	
	@Getter
	@Setter
	@Inject
	private TemplateDecorator templateDecorator;
	
//	@Inject
//	public void setGsonManager(GsonManager gm){
//		this.gsonManager = gm;
//	}
//	
//	@Inject
//	public void setTool(Tools tool){
//		this.tool = tool;
//	}
//	
//	@Inject
//	public void setMathTool(MathTool mt){
//		this.mathTool = mt;
//	}
//	
//	@Inject
//	public void setTemplate(MathTool mt){
//		this.mathTool = mt;
//	}
	
	
	

}