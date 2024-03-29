package ar.edu.unnoba.Proyecto.controller;

import ar.edu.unnoba.Proyecto.model.Evento;
import ar.edu.unnoba.Proyecto.model.Subscriptor;
import ar.edu.unnoba.Proyecto.service.EventoService;
import ar.edu.unnoba.Proyecto.service.SubscriptorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/visitante")
public class VisitanteController {

    private final EventoService eventoService;

    private final SubscriptorService subscriptorService;


    @Autowired
    private VisitanteController(EventoService eventoService, SubscriptorService subscriptorService) {
        this.eventoService = eventoService;
        this.subscriptorService = subscriptorService;
    }

    //*****************INICIO*****************

    @GetMapping("")
    public String redireccion() {
        return "redirect:/visitante/inicio";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "visitantes/inicio";
    }

    //*****************EVENTOS*****************

    /*
    Cada vez que se ingrese a ver los eventos, se aparecerá de forma emergente en la misma pestaña
    un formulario para almacenar un nuevo subcriptor.
    */

    @GetMapping("/eventos")
    public String eventos(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "3") int size) {
        // valores predeterminados de prueba: pagina 0 de tamaño 3
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Evento> eventoPage = eventoService.getPage(pageRequest);

        model.addAttribute("eventos", eventoPage);
        model.addAttribute("sub", new Subscriptor());
        model.addAttribute("currentPage", page); // info de la pag actual para cambiar de pagina
        model.addAttribute("totalPages", eventoPage.getTotalPages()); // cant total de paginas
        return "visitantes/eventos";
    }//FUNCIONALIDAD: Listado de todos los eventos

    @PostMapping("/eventos")
    public String eventos(@ModelAttribute("sub") Subscriptor subscriptor) {
        subscriptorService.save(subscriptor);
        return "redirect:/visitante/eventos";
    }

    //*****************EVENTO (AL SELECCIONAR CLICKEANDO)*****************

    @GetMapping("/evento/{id}")
    public String evento(@PathVariable Long id, Model model) {

        Evento evento = eventoService.get(id);
        model.addAttribute("evento", evento);
        return "visitantes/evento";
    }//FUNCIONALIDAD: Mostrar en detalle un Evento

    //*****************CONTACTO*****************

    @GetMapping("/contacto")
    public String mostrarFormulario() {
        return "visitantes/contacto";
    }//FUNCIONALIDAD: muestra la vista de contacto con su formulario

    @PostMapping("/contacto")
    public String recibirMensaje() {
        return "redirect:/visitante/inicio";
    }

    @GetMapping("/historia")
    public String historia() {
        return "visitantes/historia";
    }

}
