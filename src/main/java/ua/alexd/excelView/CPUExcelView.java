package ua.alexd.excelView;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;
import ua.alexd.domain.CPU;
import ua.alexd.domain.SSD;
import ua.alexd.domain.ShopDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static ua.alexd.excelView.RowStyleProvider.*;
import static ua.alexd.util.DateTimeProvider.getCurrentDateTime;

@Component("cpuExcelView")
public class CPUExcelView extends AbstractXlsxView implements ExcelFileStructure {
    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        List<ShopDomain> types = (List<ShopDomain>) model.get("cpus");
        var currentDateTime = getCurrentDateTime();
        var sheet = workbook.createSheet("CPUs sheet");
        sheet.setFitToPage(true);

        wipePreviousStyles();
        setExcelHeader(workbook, sheet);
        setExcelRows(workbook, sheet, types);

        response.setHeader("Content-Disposition", "attachment; filename=cpus-sheet " + currentDateTime + ".xlsx");
    }

    @Override
    public void setExcelHeader(@NotNull Workbook workbook, @NotNull Sheet excelSheet) {
        var header = excelSheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Модель");
        header.createCell(2).setCellValue("Частота(GHz)");
        setHeaderRowStyle(workbook, header, excelSheet);
    }

    @Override
    public void setExcelRows(@NotNull Workbook workbook, @NotNull Sheet excelSheet, @NotNull List<ShopDomain> rows) {
        var rowCount = 1;
        for (var row : rows) {
            var cpuRow = (CPU) row;
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(cpuRow.getId());
            generalRow.createCell(1).setCellValue(cpuRow.getModel());
            generalRow.createCell(2).setCellValue(cpuRow.getFrequency());
            setGeneralRowStyle(workbook, generalRow);
        }
    }
}