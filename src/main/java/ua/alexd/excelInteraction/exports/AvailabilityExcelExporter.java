package ua.alexd.excelInteraction.exports;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ua.alexd.domain.Availability;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Service("availabilityExcelView")
@Lazy
public class AvailabilityExcelExporter extends ExcelExporter {
    public AvailabilityExcelExporter(RowsStylerBuilder rowsStylerBuilder) {
        super(rowsStylerBuilder);
    }

    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        @SuppressWarnings("unchecked") List<Availability> availabilities = (List<Availability>) model.get("availabilities");
        var currentDateTime = timeProvider.getCurrentDateTime();
        var sheet = workbook.createSheet("Availability sheet");
        sheet.setFitToPage(true);

        var styler = rowsStylerBuilder.getRowStyler(workbook);
        setExcelHeader(sheet, styler);
        setExcelRows(sheet, availabilities, styler);

        response.setHeader("Content-Disposition", "attachment; filename=availability-sheet " + currentDateTime + ".xlsx");
    }

    public void setExcelHeader(@NotNull Sheet excelSheet, @NotNull RowsStyler styler) {
        var header = excelSheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Модель");
        header.createCell(2).setCellValue("Ціна");
        header.createCell(3).setCellValue("Кількість");
        header.createCell(4).setCellValue("Номер магазину");
        header.createCell(5).setCellValue("Адреса магазину");
        header.createCell(6).setCellValue("Початок продаж");
        header.createCell(7).setCellValue("Закінчення продаж");
        styler.setHeaderRowStyle(header, excelSheet);
    }

    public void setExcelRows(@NotNull Sheet excelSheet, @NotNull List<Availability> rows, RowsStyler styler) {
        var rowCount = 1;
        for (var row : rows) {
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(row.getId());
            generalRow.createCell(1).setCellValue(row.getLaptop().getLabel().getModel());
            generalRow.createCell(2).setCellValue(row.getPrice());
            generalRow.createCell(3).setCellValue(row.getQuantity());
            generalRow.createCell(4).setCellValue(row.getShop().getId());
            generalRow.createCell(5).setCellValue(row.getShop().getAddress());
            generalRow.createCell(6).setCellValue(row.getDateStart().toString());
            generalRow.createCell(7).setCellValue(row.getDateEnd().toString());
            styler.setGeneralRowStyle(generalRow);
        }
    }
}