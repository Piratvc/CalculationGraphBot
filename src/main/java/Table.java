import com.groupdocs.conversion.Converter;
import com.groupdocs.conversion.options.convert.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Getter
@Setter
public class Table {
    private final String linkXlxFile = "Excel.xlsx";
    private final String linkPdfFile = "Pdf.pdf";
    private final String linkWordFile = "Word.docx";
    public double tableStep = 1;
    public int startT = 8;
    public int offsetHoriz = 1;
    public int offsetVertic = 1;
    public int tempinside;
    public boolean approvedHeater = false;

    public double[][] calculateHeatGraph(int t1, int t2, int tempInside, int tempOutside) {
        double deltaT0 = ((double) t1 + t2) / 2 - tempInside;
        double betaT0 = t1 - t2;
        if (startT > tempInside) {
            startT = tempInside;
        }
        int numberLines = Math.abs(-startT + tempOutside) + 1;
        double[][] arrayTable = new double[numberLines][4];
        double tempPerc = (100 / ((double) (tempInside + Math.abs(tempOutside))));

        for (int i = numberLines - 1; i >= 0; i--) {
            arrayTable[i][3] = (100 - (tempPerc * (numberLines - i - 1))) / 100;
        }

        for (int i = 0; i < numberLines; i++) {
            arrayTable[i][0] = startT - i;
            arrayTable[i][1] = Math.round(tempInside + deltaT0 * Math.pow(arrayTable[i][3], 0.8) + 0.5 * betaT0 * arrayTable[i][3]);
            arrayTable[i][2] = Math.round(tempInside + deltaT0 * Math.pow(arrayTable[i][3], 0.8) - 0.5 * betaT0 * arrayTable[i][3]);
            arrayTable[i][3] = Math.round(arrayTable[i][3] * 100);
        }
        return arrayTable;
    }

    public double[][] calculateHeatGraph(int t1, int t2, int t21, int t22, int tIns, int tOut) {
        double[][] arrayTable1 = calculateHeatGraph(t1, t2, tIns, tOut);
        double[][] arrayTable2 = calculateHeatGraph(t21, t22, tIns, tOut);
        double[][] massGraph = new double[arrayTable1.length][6];
        for (int i = 0; i < arrayTable1.length; i++) {
            massGraph[i][0] = arrayTable1[i][0];
            massGraph[i][1] = arrayTable1[i][1];
            massGraph[i][2] = arrayTable1[i][2];
            massGraph[i][3] = arrayTable2[i][1];
            massGraph[i][4] = arrayTable2[i][2];
            massGraph[i][5] = arrayTable1[i][3];
        }
        return massGraph;
    }

    public double[][] calculateHeatGraph(int t1, int t2, int t21, int t22,
                                         int t31, int t32, int tIns, int tOut) {
        double[][] arrayTable1 = calculateHeatGraph(t1, t2, tIns, tOut);
        double[][] arrayTable2 = calculateHeatGraph(t21, t22, tIns, tOut);
        double[][] massGraph3 = calculateHeatGraph(t31, t32, tIns, tOut);
        double[][] massGraph = new double[arrayTable1.length][8];
        for (int i = 0; i < arrayTable1.length; i++) {
            massGraph[i][0] = arrayTable1[i][0];
            massGraph[i][1] = arrayTable1[i][1];
            massGraph[i][2] = arrayTable1[i][2];
            massGraph[i][3] = arrayTable2[i][1];
            massGraph[i][4] = arrayTable2[i][2];
            massGraph[i][5] = massGraph3[i][1];
            massGraph[i][6] = massGraph3[i][2];
            massGraph[i][7] = arrayTable1[i][3];
        }
        return massGraph;
    }

    //метод запись в эксель, конвертация в pdf
    public File writeArrayInFile(@NotNull double[][] mass) throws FileNotFoundException {
        XSSFWorkbook workbook;
        Sheet sheet;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("График " + (int) mass[mass.length - 1][1] + "-" + (int) mass[mass.length - 1][2]);

        //стиль таблицы (базовый)
        CellStyle styleData = workbook.createCellStyle();
        styleData.setBorderBottom(BorderStyle.THIN);
        styleData.setBorderLeft(BorderStyle.THIN);
        styleData.setBorderRight(BorderStyle.THIN);
        styleData.setBorderTop(BorderStyle.THIN);
        styleData.setAlignment(HorizontalAlignment.CENTER);
        //стиль заголовка
        CellStyle styleHead = workbook.createCellStyle();
        styleHead.setBorderBottom(BorderStyle.THIN);
        styleHead.setBorderLeft(BorderStyle.THIN);
        styleHead.setBorderRight(BorderStyle.THIN);
        styleHead.setBorderTop(BorderStyle.THIN);
        styleHead.setAlignment(HorizontalAlignment.CENTER);
        //шрифт заголовка
        XSSFFont fontHead = workbook.createFont();
        fontHead.setBold(true);
        styleHead.setFont(fontHead);
        //таблица
        for (int i = 0; i < mass.length; i++) {
            Row row = sheet.createRow(offsetHoriz + 2 + i);
            for (int j = 0; j < mass[0].length; j++) {
                Cell cell = row.createCell(offsetVertic + j);
                cell.setCellValue(mass[i][j]);
                cell.setCellStyle(styleData);
            }
        }
        //обозначения иконок сверху
        Row row = sheet.createRow(offsetHoriz + 1);
        createCellExel(row, offsetVertic, "T нар.", styleHead);
        createCellExel(row, offsetVertic + 1, "T1", styleHead);
        createCellExel(row, offsetVertic + 2, "T2", styleHead);
        createCellExel(row, mass[0].length + offsetVertic - 1, "% нагр.", styleHead);
        switch (mass[0].length) {
            case 6 -> {
                createCellExel(row, offsetVertic + 3, "T1", styleHead);
                createCellExel(row, offsetVertic + 4, "T2", styleHead);
            }
            case 8 -> {
                createCellExel(row, offsetVertic + 3, "T1", styleHead);
                createCellExel(row, offsetVertic + 4, "T2", styleHead);
                createCellExel(row, offsetVertic + 5, "T1", styleHead);
                createCellExel(row, offsetVertic + 6, "T2", styleHead);
            }
        }
        //надпись наверху
        Row row1 = sheet.createRow(offsetHoriz);
        Cell cell1 = null;
        for (int i = mass[0].length + offsetVertic - 1; i >= offsetVertic; i--) {
            cell1 = row1.createCell(i);
            cell1.setCellStyle(styleHead);
        }
        String headString = switch (mass[0].length) {
            case 4 -> "График " + (int) mass[mass.length - 1][1] + "/" + (int) mass[mass.length - 1][2] +
                    " при " + (int) mass[mass.length - 1][0] + " °C, внутри " + tempinside + " °C";
            case 6 -> "График " + (int) mass[mass.length - 1][1] + "/" + (int) mass[mass.length - 1][2] +
                    " , " + (int) mass[mass.length - 1][3] + "/" + (int) mass[mass.length - 1][4] +
                    " при " + (int) mass[mass.length - 1][0] + " °C, внутри " + tempinside + " °C";
            case 8 -> "График " + (int) mass[mass.length - 1][1] + "/" + (int) mass[mass.length - 1][2] +
                    " , " + (int) mass[mass.length - 1][3] + "/" + (int) mass[mass.length - 1][4] +
                    " , " + (int) mass[mass.length - 1][5] + "/" + (int) mass[mass.length - 1][6] +
                    " при " + (int) mass[mass.length - 1][0] + " °C, внутри " + tempinside + " °C";
            default -> null;
        };
        cell1.setCellValue(headString);

        sheet.addMergedRegion(new CellRangeAddress(offsetHoriz, offsetHoriz, offsetVertic,
                mass[0].length + offsetVertic - 1));

        //шапка профиля
        if (approvedHeater) {
            //стиль шапки профиля
            CellStyle styleApproveHeater = workbook.createCellStyle();
            styleApproveHeater.setBorderBottom(BorderStyle.THIN);
            styleApproveHeater.setBorderLeft(BorderStyle.THIN);
            styleApproveHeater.setBorderRight(BorderStyle.THIN);
            styleApproveHeater.setBorderTop(BorderStyle.THIN);
            styleApproveHeater.setAlignment(HorizontalAlignment.RIGHT);
            //шрифт шапки профиля (берем с заголовка)
            styleApproveHeater.setFont(fontHead);
            //создание шапки профиля
            Row row2 = sheet.createRow(offsetHoriz + mass.length + 2);
            Cell cell2 = null;
            for (int i = mass[0].length + offsetVertic - 1; i >= offsetVertic + mass[0].length - 4; i--) {
                cell2 = row2.createCell(i);
                cell2.setCellStyle(styleApproveHeater);
            }
            //обьединение ячеек для шапки профиля
            cell2.setCellValue("Утверждено:______________________");
            sheet.addMergedRegion(new CellRangeAddress(offsetHoriz + mass.length + 2, offsetHoriz + mass.length + 2,
                    offsetVertic + mass[0].length - 4, mass[0].length + offsetVertic - 1));
        }

        //запись файла
        try {
            FileOutputStream fos = new FileOutputStream(linkXlxFile);
            workbook.write(fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при записи файл на сервер");
        }
        return ResourceUtils.getFile(linkXlxFile);
    }

    private void createCellExel(Row row, int cellNum, String text, CellStyle style) {
        Cell cell = row.createCell(cellNum);
        cell.setCellStyle(style);
        cell.setCellValue(text);
    }

    public File convertExelToPdf() {
        File pdfResult = null;
        try {
            Converter converter = new Converter(linkXlxFile);
            PdfConvertOptions convertOptions = new PdfConvertOptions();
            converter.convert(linkPdfFile, convertOptions);
            pdfResult = ResourceUtils.getFile(linkPdfFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка при конвертации в PDF");
        }
        return pdfResult;
    }

    public File convertExelToWord() {
        File wordResult = null;
        try {
            Converter converter = new Converter(linkPdfFile);
            WordProcessingConvertOptions convertOptions = new WordProcessingConvertOptions();
            converter.convert(linkWordFile, convertOptions);
            wordResult = ResourceUtils.getFile(linkWordFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Ошибка при конвертации в Word");
        }
        return wordResult;
    }
}
