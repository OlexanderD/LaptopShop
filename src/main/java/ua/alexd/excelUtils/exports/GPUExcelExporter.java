package ua.alexd.excelUtils.exports;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;
import ua.alexd.domain.GPU;
import ua.alexd.domain.ShopDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static ua.alexd.excelUtils.exports.RowStyleProvider.*;
import static ua.alexd.dateTimeUtils.DateTimeProvider.getCurrentDateTime;

@Component("gpuExcelView")
public class GPUExcelExporter extends AbstractXlsxView implements ExcelExportStructure {
    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        List<ShopDomain> gpus = (List<ShopDomain>) model.get("gpus");
        var currentDateTime = getCurrentDateTime();
        var sheet = workbook.createSheet("GPUs sheet");
        sheet.setFitToPage(true);

        wipePreviousStyles();
        setExcelHeader(workbook, sheet);
        setExcelRows(workbook, sheet, gpus);

        response.setHeader("Content-Disposition", "attachment; filename=gpus-sheet " + currentDateTime + ".xlsx");
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
    public void setExcelRows(@NotNull Workbook workbook, @NotNull Sheet excelSheet, @NotNull List<ShopDomain> rows) {
        var rowCount = 1;
        for (var row : rows) {
            var gpuRow = (GPU) row;
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(gpuRow.getId());
            generalRow.createCell(1).setCellValue(gpuRow.getModel());
            generalRow.createCell(2).setCellValue(gpuRow.getMemory());
            setGeneralRowStyle(workbook, generalRow);
        }
    }
}