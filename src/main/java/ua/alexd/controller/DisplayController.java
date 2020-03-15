package ua.alexd.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.alexd.domain.Display;
import ua.alexd.excelUtils.imports.DisplayExcelImporter;
import ua.alexd.repos.DisplayRepo;

import java.io.IOException;

import static ua.alexd.excelUtils.imports.UploadedFilesManager.deleteNonValidFile;
import static ua.alexd.excelUtils.imports.UploadedFilesManager.saveUploadingFile;
import static ua.alexd.inputUtils.inputValidator.stringContainsAlphabet;
import static ua.alexd.specification.DisplaySpecification.*;

@Controller
@RequestMapping("/display")
public class DisplayController {
    private final DisplayRepo displayRepo;
    private Iterable<Display> lastOutputtedDisplay;

    private final DisplayExcelImporter excelImporter;

    public DisplayController(DisplayRepo displayRepo, DisplayExcelImporter excelImporter) {
        this.displayRepo = displayRepo;
        this.excelImporter = excelImporter;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    @GetMapping
    private String getRecords(@RequestParam(required = false) String model,
                              @RequestParam(required = false) String type,
                              @RequestParam(required = false) String diagonal,
                              @RequestParam(required = false) String resolution,
                              @NotNull Model siteModel) {
        var displaySpecification = Specification.where(modelLike(model)).and(typeEqual(type))
                .and(diagonalEqual(diagonal)).and(resolutionEqual(resolution));
        var displays = displayRepo.findAll(displaySpecification);
        lastOutputtedDisplay = displays;
        siteModel.addAttribute("displays", displays);
        return "/list/displayList";
    }

    @NotNull
    @GetMapping("/add")
    private String addRecord() {
        return "add/displayAdd";
    }

    @NotNull
    @PostMapping("/add")
    private String addRecord(@NotNull @ModelAttribute("newDisplay") Display newDisplay, @NotNull Model model) {
        if (!saveRecord(newDisplay)) {
            model.addAttribute("errorMessage", "Представлена модель уже присутня в базі!");
            return "add/displayAdd";
        }
        return "redirect:/display";
    }

    @NotNull
    @GetMapping("/edit/{editDisplay}")
    private String editRecord(@NotNull @PathVariable Display editDisplay, @NotNull Model model) {
        model.addAttribute("editDisplay", editDisplay);
        return "/edit/displayEdit";
    }

    @NotNull
    @PostMapping("/edit/{editDisplay}")
    private String editRecord(@RequestParam String model, @RequestParam String type,
                              @RequestParam String diagonal, @RequestParam String resolution,
                              @NotNull @PathVariable Display editDisplay, @NotNull Model siteModel) {
        editDisplay.setModel(model);
        editDisplay.setType(type);
        editDisplay.setDiagonal(diagonal);
        editDisplay.setResolution(resolution);
        if (!saveRecord(editDisplay)) {
            siteModel.addAttribute("errorMessage", "Представлена модель уже присутня в базі!");
            return "edit/displayEdit";
        }
        return "redirect:/display";
    }

    @NotNull
    @GetMapping("/importExcel")
    private String importExcel(@NotNull Model model) {
        initializeImportAttributes(model);
        return "parts/excelFilesUpload";
    }

    @NotNull
    @PostMapping("/importExcel")
    private String importExcel(@NotNull @RequestParam MultipartFile uploadingFile, @NotNull Model model)
            throws IOException {
        var displayFilePath = "";
        try {
            displayFilePath = saveUploadingFile(uploadingFile);
            var newDisplays = excelImporter.importFile(displayFilePath);
            newDisplays.forEach(this::saveRecord);
            return "redirect:/display";
        } catch (IllegalArgumentException ignored) {
            deleteNonValidFile(displayFilePath);
            model.addAttribute("errorMessage", "Завантажено некоректний файл для таблиці дисплеїв!");
            initializeImportAttributes(model);
            return "parts/excelFilesUpload";
        }
    }

    private static void initializeImportAttributes(@NotNull Model model) {
        model.addAttribute("controllerName", Display.class.getSimpleName());
        model.addAttribute("tableName", "дисплеїв");
    }

    @NotNull
    @GetMapping("/exportExcel")
    private String exportExcel(@NotNull Model model) {
        model.addAttribute("displays", lastOutputtedDisplay);
        return "displayExcelView";
    }

    @NotNull
    @GetMapping("/delete/{delDisplay}")
    private String deleteRecord(@NotNull @PathVariable Display delDisplay) {
        displayRepo.delete(delDisplay);
        return "redirect:/display";
    }

    private boolean saveRecord(Display saveDisplay) {
        try {
            displayRepo.save(saveDisplay);
        } catch (DataIntegrityViolationException ignored) {
            return false;
        }
        return true;
    }
}