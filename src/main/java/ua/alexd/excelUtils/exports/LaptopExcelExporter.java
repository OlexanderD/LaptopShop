package ua.alexd.excelUtils.exports;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;
import ua.alexd.domain.Laptop;
import ua.alexd.domain.ShopDomain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static ua.alexd.dateTimeUtils.DateTimeProvider.getCurrentDateTime;

@Component("laptopExcelView")
public class LaptopExcelExporter extends AbstractXlsxView implements ExcelExportStructure {
    @Override
    protected void buildExcelDocument(@NotNull Map<String, Object> model, @NotNull Workbook workbook,
                                      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        @SuppressWarnings("unchecked") List<ShopDomain> laptops = (List<ShopDomain>) model.get("laptops");
        var currentDateTime = getCurrentDateTime();
        var sheet = workbook.createSheet("Laptops sheet");
        sheet.setFitToPage(true);

        var styler = new RowsStyler(workbook);
        setExcelHeader(sheet, styler);
        setExcelRows(sheet, laptops, styler);

        response.setHeader("Content-Disposition", "attachment; filename=laptops-sheet " + currentDateTime + ".xlsx");
    }

    @Override
    public void setExcelHeader(@NotNull Sheet excelSheet, @NotNull RowsStyler styler) {
        var header = excelSheet.createRow(0);
        header.createCell(0).setCellValue("Id");
        header.createCell(1).setCellValue("Бренд");
        header.createCell(2).setCellValue("Модель");
        header.createCell(3).setCellValue("Тип");
        header.createCell(4).setCellValue("Збірка");
        styler.setHeaderRowStyle(header, excelSheet);
    }

    @Override
    public void setExcelRows(@NotNull Sheet excelSheet, @NotNull List<ShopDomain> rows, RowsStyler styler) {
        var rowCount = 1;
        for (var row : rows) {
            var laptopRow = (Laptop) row;
            var generalRow = excelSheet.createRow(rowCount++);
            generalRow.createCell(0).setCellValue(laptopRow.getId());
            generalRow.createCell(1).setCellValue(laptopRow.getLabel().getBrand());
            generalRow.createCell(2).setCellValue(laptopRow.getLabel().getModel());
            generalRow.createCell(3).setCellValue(laptopRow.getType() != null
                    ? laptopRow.getType().getName()
                    : "Відсутній");
            generalRow.createCell(4).setCellValue(laptopRow.getHardware() != null
                    ? laptopRow.getHardware().getAssemblyName()
                    : "Відсутній");
            styler.setGeneralRowStyle(generalRow);
        }
    }
}