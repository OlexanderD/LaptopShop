package ua.alexd.excelUtils.exports;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;
import ua.alexd.domain.Hardware;
import ua.alexd.domain.ShopDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static ua.alexd.excelUtils.exports.RowStyleProvider.*;
import static ua.alexd.dateTimeUtils.DateTimeProvider.getCurrentDateTime;

@Component("hardwareExcelView")
public class HardwareExcelExporter extends AbstractXlsxView implements ExcelExportStructure {
    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        List<ShopDomain> hardware = (List<ShopDomain>) model.get("hardware");
        var currentDateTime = getCurrentDateTime();
        var sheet = workbook.createSheet("Hardware sheet");
        sheet.setFitToPage(true);

        wipePreviousStyles();
        setExcelHeader(workbook, sheet);
        setExcelRows(workbook, sheet, hardware);

        response.setHeader("Content-Disposition", "attachment; filename=hardware-sheet " + currentDateTime + ".xlsx");
    }

    @Override
    public void setExcelHeader(@NotNull Workbook workbook, @NotNull Sheet excelSheet) {
        var header = excelSheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Назва збірки");
        header.createCell(2).setCellValue("Модель процесора");
        header.createCell(3).setCellValue("Модель відеокарти");
        header.createCell(4).setCellValue("Модель дисплею");
        header.createCell(5).setCellValue("Модель оперативної пам'яті");
        header.createCell(6).setCellValue("Модель SSD диску");
        header.createCell(7).setCellValue("Модель HDD диску");
        setHeaderRowStyle(workbook, header, excelSheet);
    }

    @Override
    public void setExcelRows(@NotNull Workbook workbook, @NotNull Sheet excelSheet, @NotNull List<ShopDomain> rows) {
        var rowCount = 1;
        for (var row : rows) {
            var hardwareRow = (Hardware) row;
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(hardwareRow.getId());
            generalRow.createCell(1).setCellValue(hardwareRow.getAssemblyName());
            generalRow.createCell(2).setCellValue(hardwareRow.getCpu() != null
                    ? hardwareRow.getCpu().getModel()
                    : "Відсутній");
            generalRow.createCell(3).setCellValue(hardwareRow.getGpu() != null
                    ? hardwareRow.getGpu().getModel()
                    : "Відсутній");
            generalRow.createCell(4).setCellValue(hardwareRow.getDisplay() != null
                    ? hardwareRow.getDisplay().getModel()
                    : "Відсутній");
            generalRow.createCell(5).setCellValue(hardwareRow.getRam() != null
                    ? hardwareRow.getRam().getModel()
                    : "Відсутній");
            generalRow.createCell(6).setCellValue(hardwareRow.getSsd() != null
                    ? hardwareRow.getSsd().getModel()
                    : "Відсутній");
            generalRow.createCell(7).setCellValue(hardwareRow.getHdd() != null
                    ? hardwareRow.getHdd().getModel()
                    : "Відсутній");
            setGeneralRowStyle(workbook, generalRow);
        }
    }
}