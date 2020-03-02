package ua.alexd.excelView;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;
import ua.alexd.domain.SSD;
import ua.alexd.domain.ShopDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static ua.alexd.excelView.rowStyleProvider.*;
import static ua.alexd.util.DateTimeProvider.getCurrentDateTime;

@Component("ssdExcelView")
public class SSDExcelView extends AbstractXlsxView implements ExcelFileStructure {
    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        List<ShopDomain> types = (List<ShopDomain>) model.get("ssds");
        var currentDateTime = getCurrentDateTime();
        var sheet = workbook.createSheet("SSD sheet");
        sheet.setFitToPage(true);

        wipePreviousStyles();
        setExcelHeader(workbook, sheet);
        setExcelRows(workbook, sheet, types);

        response.setHeader("Content-Disposition", "attachment; filename=ssd-sheet " + currentDateTime + ".xlsx");
    }

    @Override
    public void setExcelHeader(@NotNull Workbook workbook, @NotNull Sheet excelSheet) {
        var header = excelSheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Модель");
        header.createCell(2).setCellValue("Обсяг пам'яті");
        setHeaderRowStyle(workbook, header, excelSheet);
    }

    @Override
    public void setExcelRows(@NotNull Workbook workbook, @NotNull Sheet excelSheet, List<ShopDomain> rows) {
        var rowCount = 1;
        for (var row : rows) {
            var ssdRow = (SSD) row;
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(ssdRow.getId());
            generalRow.createCell(1).setCellValue(ssdRow.getModel());
            generalRow.createCell(2).setCellValue(ssdRow.getMemory());
            setGeneralRowStyle(workbook, generalRow);
        }
    }
}