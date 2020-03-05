package ua.alexd.excelUtils.imports;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jetbrains.annotations.NotNull;
import ua.alexd.controller.RAMController;
import ua.alexd.domain.RAM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ua.alexd.excelUtils.imports.TableValidator.isValidTableStructure;

public class RAMExcelImporter extends Importer {
    @NotNull
    @Override
    public List<RAM> importFile(String uploadedFilePath)
            throws IOException, IllegalArgumentException {
        var workbook = WorkbookFactory.create(new File(uploadedFilePath));
        var ramSheet = workbook.getSheetAt(0);

        var ramTableFields = new String[]{"Id", "Модель", "Обсяг пам'яті"};
        if (isValidTableStructure(ramSheet, ramTableFields)) {
            var dataFormatter = new DataFormatter();
            var newRAMs = new ArrayList<RAM>();

            String ramModel = null;
            var modelColNum = 1;
            int ramMemory = 0;
            var memoryColNum = 2;

            for (Row row : ramSheet) {
                if (row.getRowNum() != 0)
                    for (Cell cell : row) {
                        var cellValue = dataFormatter.formatCellValue(cell);
                        if (cell.getColumnIndex() == modelColNum)
                            ramModel = cellValue;
                        else if (cell.getColumnIndex() == memoryColNum)
                            try {
                                ramMemory = Integer.parseInt(cellValue);
                            } catch (NumberFormatException ignored) { }
                    }
                if (!RAMController.isFieldsEmpty(ramModel) && ramMemory >= 1) {
                    var newRAM = new RAM(ramModel, ramMemory);
                    newRAMs.add(newRAM);

                    nullExtractedValues(ramMemory);
                }
            }
            workbook.close();
            return newRAMs;
        } else
            throw new IllegalArgumentException();
    }
}