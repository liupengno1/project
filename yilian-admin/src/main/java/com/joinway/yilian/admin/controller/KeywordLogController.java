package com.joinway.yilian.admin.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiBodyObject;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiQueryParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.joinway.framework.bean.logging.annotation.LogIgnore;
import com.joinway.framework.extension.utils.DataUtils;
import com.joinway.framework.extension.utils.NumberUtils;
import com.joinway.framework.extension.utils.data.Constructor;
import com.joinway.yilian.admin.bean.domain.KeywordLogGrid;
import com.joinway.yilian.admin.bean.domain.KeywordLogModel;
import com.joinway.yilian.admin.service.KeywordLogService;
import com.joinway.yilian.support.bean.view.BeanView;
import com.joinway.yilian.support.bean.view.PageView;
import com.joinway.yilian.support.bean.view.View;
import com.joinway.yilian.support.builder.RequestParamBuilder;
import com.joinway.yilian.support.constants.ApiConstraintConstants.Id;

/**
 * 此文件初版由工具生成，请定制开发
 * 生成时间: 2018-04-10 18:36:22
 * 
 */
@Api(name="KeywordLog维护",description="KeywordLogController")
@RestController
@RequestMapping("keywordLog")
@Validated
public class KeywordLogController {

	private static final Logger log = LoggerFactory.getLogger(KeywordLogController.class);
	
	@Autowired private KeywordLogService keywordLogService;
	
	@ApiResponseObject
	@ApiMethod(summary="查询")
	@RequestMapping(value="/search",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public PageView<List<KeywordLogGrid>> search(
		@ApiQueryParam(description="当前页号") @RequestParam(value="page",required=false,defaultValue="1") String page,
		@ApiQueryParam(description="每页行数") @RequestParam(value="rows",required=false,defaultValue="10") String rows,
		@LogIgnore HttpServletRequest request
	) throws Exception {
		int p = NumberUtils.convertToInt(page);
		int r = NumberUtils.convertToInt(rows);
		// 将请求参数数组变成单个元素，多个值用逗号分隔
		Map<String, String> params = RequestParamBuilder.buildPlainParams(request.getParameterMap());
		return keywordLogService.search(params, p == 0 ? 1 : p, r == 0 ? 1 : r);
	}
	
	@ApiResponseObject
	@ApiMethod(summary="添加")
	@RequestMapping(value="/add",method=RequestMethod.POST,produces=MediaType.APPLICATION_JSON_VALUE)
	public View add(@LogIgnore HttpServletRequest request) throws Exception {
		Map<String, String> params = RequestParamBuilder.buildPlainParams(request.getParameterMap());
		return keywordLogService.add(params);
	}

	@ApiResponseObject
	@ApiMethod(summary="加载编辑")
	@RequestMapping(value="/edit",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public BeanView<KeywordLogModel> load(@ApiQueryParam(description="id") @RequestParam("id") @Pattern(regexp=Id.Pattern,message="id格式错误") String id) throws Exception {
		return keywordLogService.load(NumberUtils.convertToInt(id));
	}
	
	@ApiResponseObject
	@ApiMethod(summary="编辑")
	@RequestMapping(value="/edit",method=RequestMethod.POST,produces=MediaType.APPLICATION_JSON_VALUE)
	public View edit(@LogIgnore HttpServletRequest request) throws Exception {
		Map<String, String> params = RequestParamBuilder.buildPlainParams(request.getParameterMap());
		return keywordLogService.edit(params);
	}
	
	@ApiResponseObject
	@ApiMethod(summary="删除")
	@RequestMapping(value="/delete",method=RequestMethod.DELETE,produces=MediaType.APPLICATION_JSON_VALUE)
	public View delete(@ApiQueryParam(description="id",format="逗号分隔主键id") @RequestParam("id") @NotBlank(message="id不能为空") String id) throws Exception {
		return keywordLogService.delete(DataUtils.splitTrimDistinct(id, ",", Constructor.Integer));
	}
	
	@ApiResponseObject
	@ApiMethod(summary="按查询条件导出")
	@RequestMapping(value="/export/query",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public View exportQuery(@ApiQueryParam(description="async",format="0-同步导出;1-异步导出") @RequestParam(value="async",defaultValue="0",required=false) String async) throws Exception {
		return keywordLogService.exportQuery(NumberUtils.convertToInt(async) == 1);
	}
	
	@ApiResponseObject
	@ApiMethod(summary="导出全部数据")
	@RequestMapping(value="/export/all",method=RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public View exportAll(@ApiQueryParam(description="async",format="0-同步导出;1-异步导出") @RequestParam(value="async",defaultValue="0",required=false) String async) throws Exception {
		return keywordLogService.exportAll(NumberUtils.convertToInt(async) == 1);
	}
	
	@ApiResponseObject
	@ApiMethod(summary="导入")
	@RequestMapping(value="/import",method=RequestMethod.POST,produces=MediaType.APPLICATION_JSON_VALUE)
	public View importExcel(
		@ApiBodyObject @RequestParam("file") MultipartFile file,
		@ApiQueryParam(description="async",format="0-同步导出;1-异步导出") @RequestParam(value="async",defaultValue="0",required=false) String async
	) throws Exception {
		return keywordLogService.importExcel(file, NumberUtils.convertToInt(async) == 1);
	}
}
