package com.example.zhonghe.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import com.example.zhonghe.pojo.data;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SheetHelper {
    static String TAG = "<<< SheetHelper >>>";

    /**
     * 导出Excel
     *
     * @param title           标题，配合 DeviceInfo 按需传入
     * @param listData        导出行数据
     * @param fileDir         导出文件夹
     * @param fileName        导出文件名
     * @param context         activity上下文
     * @param fileNameReplace 文件名称存在时，是否需要替换
     * @return
     */
    public static boolean exportExcel(String[] title, List<data> listData, String fileDir, String fileName, Context context, boolean fileNameReplace) {
        if (TextUtils.isEmpty(fileDir) || TextUtils.isEmpty(fileName) || listData == null) {
            Log.e(TAG, " 导出" + "入参不合规");
            return false;
        }
        try {
            // 创建excel xlsx格式
            Workbook wb = new XSSFWorkbook();
            // 创建工作表
            Sheet sheet = wb.createSheet();
            //创建行对象
            Row row = sheet.createRow(0);
            // 设置有效数据的行数和列数
            int colNum = title.length;   // String[] title = {"LableNo", "Status", "TID"};

            for (int i = 0; i < colNum; i++) {
                sheet.setColumnWidth(i, 20 * 256);  // 显示20个字符的宽度  列宽
                Cell cell1 = row.createCell(i);
                //第一行
                cell1.setCellValue(title[i]);
            }

            // 导入数据
            for (int rowNum = 0; rowNum < listData.size(); rowNum++) {

                // 之所以rowNum + 1 是因为要设置第二行单元格
                row = sheet.createRow(rowNum + 1);
                // 设置单元格显示宽度
                row.setHeightInPoints(28f);

                // data是数据类，这个是根据数据来进行填写
                data da = listData.get(rowNum);

                for (int j = 0; j < title.length; j++) {
                    Cell cell = row.createCell(j);
                    //要和title[]一一对应
                    switch (j) {
                        case 0:
                            cell.setCellValue(rowNum + 1);
                            break;
                        case 1:
                            cell.setCellValue(da.getTID());
                            break;
                        case 2:
                            cell.setCellValue(da.getQR());
                            break;
                        case 3:
                            cell.setCellValue(da.getBatch());
                            break;
                        case 4:
                            cell.setCellValue(da.getType());
                            break;
                        case 5:
                            cell.setCellValue(da.getComment());
                            break;
                        case 6:
                            cell.setCellValue(da.getTime());
                            break;
                        case 7:
                            cell.setCellValue(da.getCondition());
                            break;
                    }
                }

            }

            String s = Environment.getExternalStorageDirectory() + "/" + fileDir;
//            String mSDCardFolderPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileDir;
            File dir = new File(s);
            //判断文件是否存在
            if (!dir.exists()) {
                //不存在则创建
                dir.mkdirs();
            }
            File excel = new File(dir, fileName + ".xlsx");
            if (!excel.exists()) {
                excel.createNewFile();
            } else {
                if (fileNameReplace) {
                    //String newFileName = getNewFileName(getFiles(dir.getPath(), new ArrayList<>()), excel.getPath());
                    String newFileName = getXlsxNewFileName(excel);
                    excel = new File(newFileName);
                    excel.createNewFile();
                }
            }
            Log.e(TAG, " 导出路径" + excel.getPath().toString());
            FileOutputStream fos = new FileOutputStream(excel);
            wb.write(fos);
            wb.close();
            fos.flush();
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e("ExpressExcle", "exportExcel", e);
            return false;
        }
    }


    /**
     * 导入excel
     *
     * @param fileName 本地文件路径
     */
    public static List<String> readExcel(String fileName) {
        Log.d(TAG, "！！！导入路径！！！" + fileName);
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, "！！！导入失败！！！" + " 路径为空 ");
            return null;
        }
        try {
            InputStream inputStream = new FileInputStream(fileName);
            Workbook workbook;
            if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                Log.d(TAG, "！！！导入失败！！！" + " 文件格式错误 ");
                return null;
            }
            int numberOfSheets = workbook.getNumberOfSheets();
            List<String> strings = new ArrayList<>();
            for (int u = 0; u < numberOfSheets; u++) {
                Sheet sheet = workbook.getSheetAt(u);//获取表
                int rowsCount = sheet.getPhysicalNumberOfRows();//获取行数
                int lastRowNum = sheet.getLastRowNum();//获取最后一行，，从0开始
                Log.d(TAG, "行数：" + (lastRowNum + 1));
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                for (int r = 1; r <= lastRowNum; r++) {
                    String str = "";
                    Row row = sheet.getRow(r);//拿到行对象
                    if (row != null) {
                        int physicalNumberOfCells = row.getPhysicalNumberOfCells();//获取该行可用的列数、
                        short lastCellNum = row.getLastCellNum();//获取改行最后一列的列数
                        int lastCell = lastCellNum;
                        Log.d(TAG, "导入  第" + (r + 1) + "行最后一列：" + lastCell);
                        for (int i = 1; i < lastCell; i++) {
                            CellValue v0 = formulaEvaluator.evaluate(row.getCell(i));//获取单元格对象
                            if (v0 != null) {
                                CellType cellTypeEnum = v0.getCellTypeEnum();
                                if (cellTypeEnum.getCode() == 1) {
                                    //文本类型
                                    str += v0.getStringValue() + "&&";
                                } else if (cellTypeEnum.getCode() == 0) {
                                    //整数，小数类型
//                                    double numberValue = v0.getNumberValue();
                                    String stringValue = new NumberEval(v0.getNumberValue()).getStringValue();
                                    str += stringValue + "&&";
                                } else {
                                    //其他类型，暂时不解析
                                }
                            }
                        }
                        Log.d(TAG, "导入  第" + (r + 1) + "行  内容：" + str);
                        strings.add(str);
                    } else {
                        Log.d(TAG, "第 " + (r + 1) + " 行没有可用表格，跳过");
                        continue;
                    }
                }
            }
            workbook.close();
            return strings;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 导出PDF
     *
     * @param view            要导出的view，如果view高度过高（超过一屏的高度），在改view外部套一层Scrollview即可
     * @param fileDir         导出文件夹
     * @param fileName        导出文件名称
     * @param fileNameReplace 文件名称存在时，是否需要替换
     * @return
     */
    public static boolean createPdfFromView(View view, String fileDir, String fileName, boolean fileNameReplace) {
        try {
            if (view == null || fileDir == null || fileName == null) {
                Log.e(TAG, "导出PDF" + "入参为空");
                return false;
            }
            String s = Environment.getExternalStorageDirectory() + "/" + fileDir;
//            String mSDCardFolderPath = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileDir;
            File dir = new File(s);
            //判断文件是否存在
            if (!dir.exists()) {
                //不存在则创建
                dir.mkdirs();
            }
            File pdfFile = new File(dir, fileName + ".PDF");
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            } else {
                if (fileNameReplace) {
                    String newFileName = getPDFNewFileName(pdfFile);
                    pdfFile = new File(newFileName);
                    pdfFile.createNewFile();
                }
            }

            PdfDocument document = new PdfDocument();
            //页对象
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    view.getWidth(),
                    view.getHeight(),
                    1)
                    .create();

            // 开始页
            PdfDocument.Page page = document.startPage(pageInfo);
            //绘制页
            Canvas canvas = page.getCanvas();
            view.draw(canvas);
            //结束页
            document.finishPage(page);
            //TODO  需要的话，增加更多页
            //导出文档
            FileOutputStream os = null;

            Log.i(TAG, "导出PDF" + " 开始导出，导出路径：" + pdfFile);
            os = new FileOutputStream(pdfFile);
            document.writeTo(os);
            os.close();
            Log.i(TAG, "导出PDF" + " 导出成功");
            document.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }


    private static String getXlsxNewFileName(File file) {
        if (file.exists()) {
            String newPath = file.getPath().substring(0, file.getPath().length() - 5) + "(1).xlsx";
            return getXlsxNewFileName(new File(newPath));
        } else {
            return file.getPath();
        }
    }

    private static String getPDFNewFileName(File file) {
        if (file.exists()) {
            String newPath = file.getPath().substring(0, file.getPath().length() - 4) + "(1).PDF";
            return getPDFNewFileName(new File(newPath));
        } else {
            return file.getPath();
        }
    }


}