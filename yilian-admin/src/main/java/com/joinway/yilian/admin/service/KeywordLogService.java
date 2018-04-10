package com.joinway.yilian.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.joinway.framework.data.adaptor.bean.Page;
import com.joinway.framework.data.adaptor.bean.PageRecord;
import com.joinway.framework.extension.utils.NumberUtils;
import com.joinway.framework.support.session.SessionProvider;
import com.joinway.yilian.admin.bean.domain.KeywordLogGrid;
import com.joinway.yilian.admin.bean.domain.KeywordLogModel;
import com.joinway.yilian.admin.dao.repository.KeywordLogRepository;
import com.joinway.yilian.admin.service.file.KeywordLogExcelService;
import com.joinway.yilian.admin.task.BlindTaskMonitor;
import com.joinway.yilian.admin.task.ITaskProcessor;
import com.joinway.yilian.admin.task.TaskDaemon;
import com.joinway.yilian.admin.task.TaskMonitor;
import com.joinway.yilian.data.bean.domain.KeywordLog;
import com.joinway.yilian.data.bean.domain.KeywordLogExample;
import com.joinway.yilian.data.constants.domain.KeywordLogConstants;
import com.joinway.yilian.support.bean.DiskFile;
import com.joinway.yilian.support.bean.view.BeanView;
import com.joinway.yilian.support.bean.view.FileView;
import com.joinway.yilian.support.bean.view.PageView;
import com.joinway.yilian.support.bean.view.View;
import com.joinway.yilian.support.builder.SqlParamBuilder;
import com.joinway.yilian.support.builder.ViewBuilder;
import com.joinway.yilian.support.constants.AppConstants;
import com.joinway.yilian.support.service.impl.FileService;

/**
 * 此文件初版由工具生成，请定制开发
 * 生成时间: 2018-04-10 18:36:22
 * 
 */
@Service
@Transactional(rollbackFor=Throwable.class)
public class KeywordLogService {

	private static final Logger log = LoggerFactory.getLogger(KeywordLogService.class);
	
	private static final String QUERY_NAME = "keywordLogQuery";
	
	@Autowired private KeywordLogRepository keywordLogRepository;
	@Autowired private KeywordLogExcelService keywordLogExcelService;
	@Autowired private FileService fileService;
	
	public PageView<List<KeywordLogGrid>> search(Map<String, String> params, int page, int rows) throws Exception {
		Page p = new Page(page, rows);
		
		/**
		 * buildSelectParams - 去除请求参数空值，对范围查询增加_from和_to后缀
		 * filter - 过滤不在本表中的键值
		 */
		Map<String, Object> sqlParams = filter(SqlParamBuilder.buildSelectParams(params));
		
		SessionProvider.setValue(AppConstants.Session.PreviousQueryCriteria.get(QUERY_NAME), sqlParams);	// 记录最后一次查询条件
		
		PageRecord<KeywordLogGrid> record = keywordLogRepository.find("selectGrid", sqlParams, p, KeywordLogGrid.class);
		
		return ViewBuilder.buildPageView(record);
	}
	
	public View add(Map<String, String> params) throws Exception {
		View view = new View();
		
		// TODO 重复校验
				
		Map<String, Object> sqlParams = filter(SqlParamBuilder.buildInsertParams(params));
		keywordLogRepository.insert("insert", sqlParams);
		
		return view;
	}
	
	public BeanView<KeywordLogModel> load(int id) throws Exception {
		BeanView<KeywordLogModel> view = new BeanView<>();
		
		KeywordLogModel item = keywordLogRepository.findById(id, KeywordLogModel.class);
		view.setModel(item);
		
		return view;
	}
	
	public View edit(Map<String, String> params) throws Exception {
		View view = new View();
		
		// TODO 重复校验
		int id = NumberUtils.convertToInt(params.get(KeywordLogConstants.Columns.ID));
		
		Map<String, Object> sqlParams = filter(SqlParamBuilder.buildUpdateParams(params));
		keywordLogRepository.updateById(sqlParams);
		
		return view;
	}
	
	public View delete(List<Integer> ids){
		View view = new View();
		
		KeywordLogExample e = new KeywordLogExample();
		e.createCriteria().andIdIn(ids);
		keywordLogRepository.delete(e);

		return view;
	}
	
	public FileView exportQuery(boolean async) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> sqlParams = SessionProvider.getValue(AppConstants.Session.PreviousQueryCriteria.get(QUERY_NAME), Map.class);
		return exportExcel(sqlParams, async, "导出当前查询KeywordLog");
	}
	
	public FileView exportAll(boolean async) throws Exception {
		return exportExcel(new HashMap<>(), async, "导出全部KeywordLog");
	}

	public View importExcel(MultipartFile file, boolean async) throws Exception {
		View view = new View();
		
		DiskFile df = fileService.writeUserFile(file);
		
		if(async){
			// 异步导入
			new TaskDaemon("导入KeywordLog", new ITaskProcessor(){
				@Override
				public void process(TaskMonitor monitor) throws Exception {
					keywordLogExcelService.importExcel(df, monitor);
				}
			}).start();
			
		}else{
			// 同步导入
			view = keywordLogExcelService.importExcel(df, new BlindTaskMonitor());
		}
		
		return view;
	}

	private FileView exportExcel(Map<String, Object> sqlParams, boolean async, String taskName) throws Exception {
		FileView view = new FileView();
		
		if(async){
			// 异步导出
			new TaskDaemon(taskName, new ITaskProcessor(){
				@Override
				public void process(TaskMonitor monitor) throws Exception {
					keywordLogExcelService.exportExcel(sqlParams, monitor);
				}
			}).start();
			
			view.setTaskName(taskName);
			
		}else{
			// 同步导出
			view = keywordLogExcelService.exportExcel(sqlParams, new BlindTaskMonitor());
		}
		
		return view;		
	}
	
	private Map<String, Object> filter(Map<String, Object> params){
		Map<String, Object> map = new HashMap<>();
		
		Set<Entry<String, Object>> entries = params.entrySet();
		for(Entry<String, Object> entry : entries){
			if(KeywordLogConstants.Columns.contains(entry.getKey()) || entry.getKey().endsWith(SqlParamBuilder.FROM_SUFFIX)|| entry.getKey().endsWith(SqlParamBuilder.TO_SUFFIX)){
				map.put(entry.getKey(), entry.getValue());
			}
		}
		
		return map;
	}
}
