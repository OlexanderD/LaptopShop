package ua.alexd.excelUtils.imports;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ua.alexd.controller.ClientController;
import ua.alexd.domain.Client;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static ua.alexd.excelUtils.imports.TableValidator.isValidTableStructure;

public class ClientExcelImporter extends Importer {
    @NotNull
    @Override
    public List<Client> importFile(String uploadedFilePath)
            throws IOException, IllegalArgumentException {
        var workbook = WorkbookFactory.create(new File(uploadedFilePath));
        var clientSheet = workbook.getSheetAt(0);

        var ssdTableFields = new String[]{"Id", "Ім'я", "Прізвище", "Дата реєстрації"};
        if (isValidTableStructure(clientSheet, ssdTableFields)) {
            var dataFormatter = new DataFormatter();
            var newClients = new ArrayList<Client>();

            String firstName = null;
            var firstNameColNum = 1;
            String secondName = null;
            var secondNameColNum = 2;
            Date dateReg = null;
            var dateRegColNum = 3;

            for (Row row : clientSheet) {
                if (row.getRowNum() != 0)
                    for (Cell cell : row) {
                        var cellValue = dataFormatter.formatCellValue(cell);
                        if (cell.getColumnIndex() == firstNameColNum)
                            firstName = cellValue;
                        else if (cell.getColumnIndex() == secondNameColNum)
                            secondName = cellValue;
                        else if (cell.getColumnIndex() == dateRegColNum)
                            try {
                                cellValue = normalizeDate(cellValue);
                                dateReg = new Date(new SimpleDateFormat("d-M-yy")
                                        .parse(cellValue).getTime());
                            } catch (ParseException | ArrayIndexOutOfBoundsException ignored) {
                            }
                    }
                if (!ClientController.isFieldsEmpty(firstName, secondName, dateReg)) {
                    var newClient = new Client(firstName, secondName, dateReg);
                    newClients.add(newClient);

                    nullExtractedValues(firstName, secondName, dateReg);
                }
            }
            workbook.close();
            return newClients;
        } else
            throw new IllegalArgumentException();
    }

    // TODO Eliminate doubling
    @NotNull
    @Contract(pure = true)
    private static String normalizeDate(@NotNull String initDate) {
        var dateElements = initDate.split("/");

        var temp = dateElements[0];
        dateElements[0] = dateElements[1];
        dateElements[1] = temp;

        return String.join("-", dateElements);
    }
}