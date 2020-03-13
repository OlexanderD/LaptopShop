package ua.alexd.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.alexd.domain.Hardware;
import ua.alexd.excelUtils.imports.HardwareExcelImporter;
import ua.alexd.repos.*;

import java.io.IOException;

import static ua.alexd.excelUtils.imports.UploadedFilesManager.deleteNonValidFile;
import static ua.alexd.excelUtils.imports.UploadedFilesManager.saveUploadingFile;
import static ua.alexd.inputUtils.inputValidator.stringContainsAlphabet;
import static ua.alexd.specification.HardwareSpecification.*;

@Controller
@RequestMapping("/hardware")
public class HardwareController {
    private final HardwareRepo hardwareRepo;
    private static Iterable<Hardware> lastOutputtedHardware;

    private final CPURepo cpuRepo;
    private final RAMRepo ramRepo;
    private final SSDRepo ssdRepo;
    private final DisplayRepo displayRepo;
    private final HDDRepo hddRepo;
    private final GPURepo gpuRepo;

    public HardwareController(HardwareRepo hardwareRepo, CPURepo cpuRepo, RAMRepo ramRepo, SSDRepo ssdRepo,
                              DisplayRepo displayRepo, HDDRepo hddRepo, GPURepo gpuRepo) {
        this.hardwareRepo = hardwareRepo;
        this.cpuRepo = cpuRepo;
        this.ramRepo = ramRepo;
        this.ssdRepo = ssdRepo;
        this.displayRepo = displayRepo;
        this.hddRepo = hddRepo;
        this.gpuRepo = gpuRepo;
    }

    @SuppressWarnings("ConstantConditions")
    @NotNull
    @GetMapping
    private String getRecords(@RequestParam(required = false) String displayModel,
                              @RequestParam(required = false) String displayDiagonal,
                              @RequestParam(required = false) String displayResolution,
                              @RequestParam(required = false) String displayType,
                              @RequestParam(required = false) String cpuModel,
                              @RequestParam(required = false) String cpuFrequency,
                              @RequestParam(required = false) String ramModel,
                              @RequestParam(required = false) Integer ramMemory,
                              @RequestParam(required = false) String ssdModel,
                              @RequestParam(required = false) Integer ssdMemory,
                              @RequestParam(required = false) String hddModel,
                              @RequestParam(required = false) Integer hddMemory,
                              @RequestParam(required = false) String gpuModel,
                              @RequestParam(required = false) Integer gpuMemory,
                              @RequestParam(required = false) String assemblyName,
                              @NotNull Model model) {
        var hardwareSpecification = Specification
                .where(displayModelLike(displayModel)).and(displayDiagonalEqual(displayDiagonal))
                .and(displayResolutionEqual(displayResolution)).and(displayTypeEqual(displayType))
                .and(cpuModelLike(cpuModel)).and(cpuFrequencyEqual(cpuFrequency))
                .and(gpuModelLike(gpuModel)).and(gpuMemoryEqual(gpuMemory))
                .and(ramModelLike(ramModel)).and(ramMemoryEqual(ramMemory))
                .and(ssdModelLike(ssdModel)).and(ssdMemoryEqual(ssdMemory))
                .and(hddModelLike(hddModel)).and(hddMemoryEqual(hddMemory))
                .and(assemblyNameEqual(assemblyName));
        var hardware = hardwareRepo.findAll(hardwareSpecification);
        lastOutputtedHardware = hardware;
        model.addAttribute("hardware", hardware);
        return "/list/hardwareList";
    }

    @NotNull
    @GetMapping("/add")
    private String addRecord(@NotNull Model model) {
        initializeDropDownChoices(model);
        return "add/hardwareAdd";
    }

    @NotNull
    @PostMapping("/add")
    private String addRecord(@RequestParam String assemblyName, @RequestParam String cpuModel,
                             @RequestParam String ramModel, @RequestParam String ssdModel,
                             @RequestParam String displayModel, @RequestParam String hddModel,
                             @RequestParam String gpuModel, @NotNull Model model) {
        if (!stringContainsAlphabet(assemblyName)) {
            model.addAttribute("errorMessage", "Назву нової збірки задано некоректно!");
            initializeDropDownChoices(model);
            return "add/hardwareAdd";
        }

        var cpu = cpuRepo.findByModel(cpuModel);
        var ram = ramRepo.findByModel(ramModel);
        var ssd = ssdRepo.findByModel(ssdModel);
        var hdd = hddRepo.findByModel(hddModel);
        var gpu = gpuRepo.findByModel(gpuModel);
        var display = displayRepo.findByModel(displayModel);

        var newHardware = new Hardware(assemblyName, cpu, gpu, ram, ssd, hdd, display);
        if (!saveRecord(newHardware, model))
            return "add/hardwareAdd";

        return "redirect:/hardware";
    }

    @NotNull
    @GetMapping("/edit/{editHardware}")
    private String editRecord(@PathVariable Hardware editHardware, @NotNull Model model) {
        model.addAttribute("editHardware", editHardware);
        initializeDropDownChoices(model);
        return "/edit/hardwareEdit";
    }

    @NotNull
    @PostMapping("/edit/{editHardware}")
    private String editRecord(@RequestParam String assemblyName, @RequestParam String cpuModel,
                              @RequestParam String ramModel, @RequestParam String ssdModel,
                              @RequestParam String displayModel, @RequestParam String hddModel,
                              @RequestParam String gpuModel, @PathVariable Hardware editHardware,
                              @NotNull Model model) {
        if (!stringContainsAlphabet(assemblyName)) {
            model.addAttribute("errorMessage", "Назву змінюваної збірки задано некоректно!");
            initializeDropDownChoices(model);
            return "/edit/hardwareEdit";
        }

        editHardware.setAssemblyName(assemblyName);

        var cpu = cpuRepo.findByModel(cpuModel);
        editHardware.setCpu(cpu);

        var gpu = gpuRepo.findByModel(gpuModel);
        editHardware.setGpu(gpu);

        var ram = ramRepo.findByModel(ramModel);
        editHardware.setRam(ram);

        var ssd = ssdRepo.findByModel(ssdModel);
        editHardware.setSsd(ssd);

        var hdd = hddRepo.findByModel(hddModel);
        editHardware.setHdd(hdd);

        var display = displayRepo.findByModel(displayModel);
        editHardware.setDisplay(display);

        if (!saveRecord(editHardware, model))
            return "/edit/hardwareEdit";

        return "redirect:/hardware";
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
        var hardwareFilePath = "";
        try {
            hardwareFilePath = saveUploadingFile(uploadingFile);
            // TODO Rework in DI
            var importer = new HardwareExcelImporter(cpuRepo, ramRepo, ssdRepo, displayRepo, hddRepo, gpuRepo);
            var newHardware = importer.importFile(hardwareFilePath);
            newHardware.forEach(newAssembly -> saveRecord(newAssembly, model));
            return "redirect:/hardware";
        } catch (IllegalArgumentException ignored) {
            deleteNonValidFile(hardwareFilePath);
            model.addAttribute("errorMessage", "Завантажено некоректний файл для таблиці збірок!");
            initializeImportAttributes(model);
            return "parts/excelFilesUpload";
        }
    }

    private static void initializeImportAttributes(@NotNull Model model) {
        model.addAttribute("controllerName", Hardware.class.getSimpleName());
        model.addAttribute("tableName", "збірок");
    }

    @NotNull
    @GetMapping("/exportExcel")
    private String exportExcel(@NotNull Model model) {
        model.addAttribute("hardware", lastOutputtedHardware);
        return "hardwareExcelView";
    }

    @NotNull
    @GetMapping("/delete/{delHardware}")
    private String deleteRecord(@NotNull @PathVariable Hardware delHardware) {
        hardwareRepo.delete(delHardware);
        return "redirect:/hardware";
    }

    private boolean saveRecord(Hardware saveHardware, Model model) {
        try {
            hardwareRepo.save(saveHardware);
        } catch (DataIntegrityViolationException ignored) {
            model.addAttribute("errorMessage",
                    "Збірка " + saveHardware.getAssemblyName() + " уже присутня в базі");
            initializeDropDownChoices(model);
            return false;
        }
        return true;
    }

    private void initializeDropDownChoices(@NotNull Model model) {
        model.addAttribute("cpuModels", cpuRepo.getAllModels())
                .addAttribute("ramModels", ramRepo.getAllModels())
                .addAttribute("ssdModels", ssdRepo.getAllModels())
                .addAttribute("displayModels", displayRepo.getAllModels())
                .addAttribute("hddModels", hddRepo.getAllModels())
                .addAttribute("gpuModels", gpuRepo.getAllModels());
    }
}