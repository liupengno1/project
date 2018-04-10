package com.joinway.yilian.admin.service.file;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joinway.framework.extension.utils.DateTimeUtils;
import com.joinway.framework.extension.utils.NumberUtils;
import com.joinway.yilian.admin.bean.domain.KeywordLogGrid;
import com.joinway.yilian.admin.dao.repository.KeywordLogRepository;
import com.joinway.yilian.admin.task.TaskMonitor;
import com.joinway.yilian.data.bean.domain.KeywordLog;
import com.joinway.yilian.data.constants.domain.KeywordLogConstants;
import com.joinway.yilian.data.constants.domain.TaskConstants;
import com.joinway.yilian.support.bean.DiskFile;
import com.joinway.yilian.support.bean.view.BeanView;
import com.joinway.yilian.support.bean.view.FileView;
import com.joinway.yilian.support.service.impl.FilePathBuilder;
import com.joinway.yilian.support.utils.ExcelUtils;
import com.joinway.yilian.support.utils.Utils;

/**
 * 此文件初版由工具生成，请定制开发
 * 生成时间: 2018-04-10 18:36:22
 * 
 */
@Service
@Transactional(rollbackFor=Throwable.class)
public class KeywordLogExcelService {

	private static final Logger log = LoggerFactory.getLogger(KeywordLogExcelService.class);
	
	private static final short DOWNLOAD_UPDATE_INTERVAL = 100;
	private static final short UPLOAD_UPDATE_INTERVAL = 10;
	
	@Autowired private FilePathBuilder filePathBuilder;
	@Autowired private KeywordLogRepository keywordLogRepository;

	public FileView exportExcel(Map<String, Object> sqlParams, TaskMonitor monitor) throws Exception {
		FileView view = new FileView();
		
		List<KeywordLogGrid> records = keywordLogRepository.find("selectGrid", sqlParams, KeywordLogGrid.class);
		
		if(CollectionUtils.isNotEmpty(records)){
			// 标题
			List<String> heads = Arrays.asList(new String[]{
				"序号"
				, "关键词"
				, "创建人id"
				, "创建日期"
				, "创建日期"
				, "更新日期"
				, "更新人"
				, "服务id"
			});
			
			// 数据行
			List<List<String>> rows = new ArrayList<>();
			for(int i = 0; i < records.size(); i++){
				KeywordLogGrid record = records.get(i);
				List<String> list = new ArrayList<>();
				
				list.add(String.valueOf(i + 1));
				list.add(ExcelUtils.getStringValue(record.getKeyword()));
				list.add(ExcelUtils.getStringValue(record.getCreateUserId()));
				list.add(ExcelUtils.getStringValue(record.getCreateBy()));
				list.add(ExcelUtils.formatDateTime(record.getCreateDate()));
				list.add(ExcelUtils.formatDateTime(record.getUpdateDate()));
				list.add(ExcelUtils.getStringValue(record.getUpdateBy()));
				list.add(ExcelUtils.getStringValue(record.getServiceId()));
				
				rows.add(list);
				
				if(i % DOWNLOAD_UPDATE_INTERVAL == 0){
					monitor.setPercent(NumberUtils.convertToShort((i + 1) * TaskMonitor.HUNDRED / records.size()));
				}
				if(monitor.isStopped()){
					break;
				}
			}
			
			if(!monitor.isStopped()){
				String traceCode = Utils.getTraceCode().getThreadCode();
				if(StringUtils.isBlank(traceCode)){
					traceCode = UUID.randomUUID().toString().split("-")[0];
				}
				DiskFile excel = filePathBuilder.buildArchivePath(DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmss_") + traceCode + ".xlsx");
				File dir = new File(FilenameUtils.getFullPath(excel.getLocalPath()));
				if(!dir.exists()){
					dir.mkdirs();
				}
				ExcelUtils.export(heads, rows, excel.getLocalPath());
				
				view.setUrl(excel.getHttpPath());
				
				monitor.setUrlPath(excel.getHttpPath());
				monitor.setFinishStatus(TaskConstants.Values.FinishStatus.Success);
			}else{
				monitor.setFinishStatus(TaskConstants.Values.FinishStatus.Abort);
			}
		}
		
		return view;		
	}

	public BeanView<List<String>> importExcel(DiskFile df, TaskMonitor monitor) throws Exception {
		BeanView<List<String>> view = new BeanView<>();
		
		Workbook wb = new XSSFWorkbook(new FileInputStream(df.getLocalPath()));
		Sheet sheet = wb.getSheetAt(0);
		Iterator<Row> rows = sheet.rowIterator();
		
		List<List<String>> lines = new ArrayList<>();
		int rowIndex = 0;
		while(rows.hasNext()){
			Row row = rows.next();
			
			if(rowIndex++ == 0){
				// 跳过标题行
				continue;
			}
			
			// 读取所有行
			int column = 0;
			String value = ExcelUtils.getCellValue(row.getCell(column++));
			if(StringUtils.isBlank(value)){
				// 第一列没有值，认为数据读取完毕
				break;
			}
			
			// 从第二列开始读取
			List<String> line = new ArrayList<>();
			
			for(String columnName : KeywordLogConstants.Columns.COLUMNS){
				if(!KeywordLogConstants.Columns.ID.equals(columnName.toLowerCase()) && !KeywordLogConstants.Columns.AUDIT_COLUMNS.contains(columnName.toLowerCase())){
					line.add(ExcelUtils.getCellValue(row.getCell(column++)));
				}
			}
			
			lines.add(line);
		}
		
		wb.close();

		do{
			if(CollectionUtils.isEmpty(lines)){
				break;
			}
			
			// 校验单元格数据格式
			List<String> violations = validateFormats(lines);
			if(CollectionUtils.isNotEmpty(violations)){
				view.setModel(violations);
				break;
			}
			
			// 更新数据库
			for(int i = 0; i < lines.size(); i++){
				List<String> line = lines.get(i);
				
				int index = 0;
				Map<String, Object> params = new HashMap<>();
				
				KeywordLog record = checkExistence(line);
				if(record != null){
					params.put(KeywordLogConstants.Columns.ID, record.getId());
				}
				
				params.put(KeywordLogConstants.Columns.KEYWORD, line.get(index++));
				params.put(KeywordLogConstants.Columns.SERVICE_ID, line.get(index++));

				if(record != null){
					// 更新
					keywordLogRepository.updateById(params);
				}else{
					// 插入
					keywordLogRepository.insert(params);
				}
				
				if(i % UPLOAD_UPDATE_INTERVAL == 0){
					monitor.setPercent(NumberUtils.convertToShort((i + 1) * TaskMonitor.HUNDRED / lines.size()));
				}
				if(monitor.isStopped()){
					break;
				}
			}
		}while(false);
		
		if(!monitor.isStopped()){
			monitor.setUrlPath(df.getHttpPath());
			monitor.setFinishStatus(TaskConstants.Values.FinishStatus.Success);
		}else{
			monitor.setFinishStatus(TaskConstants.Values.FinishStatus.Abort);
		}
		
		return view;
	}
	
	/**
	 * 校验excel行内容是否有格式错误
	 * @param lines
	 * @return	错误消息
	 */
	private List<String> validateFormats(List<List<String>> lines){
		return null;
	}
	
	/**
	 * 检查数据库中是否有重复记录
	 * @param line
	 * @return	重复的记录
	 */
	private KeywordLog checkExistence(List<String> line){
		return null;
	}
}
