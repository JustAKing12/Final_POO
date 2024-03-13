package ar.edu.unnoba.Proyecto.controller;

import ar.edu.unnoba.Proyecto.exceptionHandler.EventoNotFoundException;
//import ar.edu.unnoba.Proyecto.model.Actividad;
import ar.edu.unnoba.Proyecto.model.Evento;
import ar.edu.unnoba.Proyecto.model.Usuario;
//import ar.edu.unnoba.Proyecto.service.ActividadService;
import ar.edu.unnoba.Proyecto.service.EnviarMailService;
import ar.edu.unnoba.Proyecto.service.EventoService;
import ar.edu.unnoba.Proyecto.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLException;

@Controller
@RequestMapping("/administrador")
public class AdministradorController {

    private final EventoService eventoService;

    private final UsuarioService usuarioService;

    private final EnviarMailService enviarMailService;


    @Autowired
    public AdministradorController(EventoService eventoService, UsuarioService usuarioService, EnviarMailService enviarMailService) {
        this.eventoService = eventoService;
        this.usuarioService = usuarioService;
        this.enviarMailService = enviarMailService;
    }

    //*****************INDEX*****************

    @GetMapping("/inicio")
    public String index(Model model, Authentication authentication) {
        User sessionUser = (User) authentication.getPrincipal();
        model.addAttribute("user", sessionUser); //Se añade usuario para mostrar su nombre.
        return "administradores/inicio";
    }

    //*****************EVENTOS*****************

    @GetMapping("/eventos")
    public String eventos(Model model, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "9") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Evento> eventoPage = eventoService.getPage(pageRequest);

        model.addAttribute("eventos", eventoPage);
        model.addAttribute("currentPage", page); // info de la pag actual para cambiar de pagina
        model.addAttribute("totalPages", eventoPage.getTotalPages()); // cant total de paginas
        return "administradores/eventos";
    }//FUNCIONALIDAD: muestra los eventos con los usuarios que los creó

    @GetMapping("/eventos/eliminar/{id}")
    public String eliminarEvento(@PathVariable Long id) {
        eventoService.delete(id);
        return "redirect:/administrador/eventos";
    }//FUNCIONALIDAD: elimina un evento por su id

    @GetMapping("/eventos/nuevo")
    public String nuevoEvento(Model model, Authentication authentication) {
        User sessionUser = (User) authentication.getPrincipal();

        Evento evento = new Evento();
        model.addAttribute("evento", evento); //el usuario debe introducir: titulo, descripcion, imagen
        model.addAttribute("user", sessionUser);
        return "administradores/nuevo-evento";
    }//FUNCIONALIDAD: muestra el formulario para crear un nuevo evento

    @PostMapping("/eventos/nuevo")
    public String crearEvento(Model model, Authentication authentication, @Valid Evento evento, BindingResult result) {
        User sessionUser = (User) authentication.getPrincipal();

        if (result.hasErrors()) {
            model.addAttribute("evento", evento);
            model.addAttribute("user", sessionUser);
            return "administradores/nuevo-evento";

        }//Mantiene los datos que ingresó el usuario si vuelve al mismo html
        evento.setUsuario(usuarioService.findByUserName(sessionUser.getUsername()));
        eventoService.save(evento);
        enviarMailService.enviar(evento);
        model.addAttribute("success", "El evento ha sido creado correctamente.");
        return "redirect:/administrador/inicio";
    }//FUNCIONALIDAD: procesa el formulario de creación de un nuevo evento y lo guarda

    //*****************EVENTO (AL SELECCIONAR CLICKEANDO)*****************

    @GetMapping("/evento/{id}")
    public String modificarEvento(Model model, Authentication authentication, @PathVariable Long id) {

        User sessionUser = (User) authentication.getPrincipal();

        Evento evento = eventoService.get(id);
        model.addAttribute("evento", evento);
        model.addAttribute("user", sessionUser);
        model.addAttribute("mensaje", "los eventos se modificaran");
        return "administradores/evento";
    }//FUNCIONALIDAD: muestra un evento específico con sus detalles y permite modificarlo

    @PostMapping("/evento/{id}")
    //FUNCIONALIDAD: procesa el formulario de modificación de un evento y guarda los cambios
    public String modificarEvento(Model model, Authentication authentication, @Valid Evento evento, BindingResult result, @RequestParam("imagen") MultipartFile imagen) {
        User sessionUser = (User) authentication.getPrincipal();

        // guarda el evento existente
        Evento eventoExistente = eventoService.get(evento.getId());

        // Verifica si se cargo una nueva imagen
        if (!imagen.isEmpty()) {
            try {
                // Actualizar la imagen del evento
                eventoExistente.setImagen(imagen);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("evento", evento);
            model.addAttribute("user", sessionUser);
            return "administradores/evento";//Entra aca como si hubiera un error
            //Mantiene los datos que ingresó el usuario, aunque fuera error, para luego corregirlos al ingresar de nuevo.
        }

        eventoExistente.setUsuario(usuarioService.findByUserName(sessionUser.getUsername()));
        eventoExistente.setTitulo(evento.getTitulo());
        eventoExistente.setDescripcion(evento.getDescripcion());

        eventoService.save(eventoExistente);
        model.addAttribute("success", "El evento ha sido modificado correctamente.");
        return "redirect:/administrador/eventos";
    }//FUNCIONALIDAD: procesa el formulario de modificación de un evento y guarda los cambios


    //*****************HISTORIA*****************

    @GetMapping("/historia")
    public String historia(){
        return "administradores/historia";
    }

    //*****************CONTACTO*****************

    @GetMapping("/contacto")
    public String contacto(Model model, Authentication authentication) {
        User sessionUser = (User) authentication.getPrincipal();
        model.addAttribute("user", sessionUser);
        return "administradores/contacto";
    }

    //*****************USUARIOS*****************

    @GetMapping("/usuario/crear")
    public String crearUsuario(Model model, Authentication authentication) {
        User sessionUser = (User) authentication.getPrincipal();

        Usuario usuario = new Usuario();
        model.addAttribute("usuario", usuario);
        model.addAttribute("user", sessionUser);
        model.addAttribute("crear", 1);
        return "administradores/nuevo-usuario";
    }

    @PostMapping("/usuario/crear")
    public String crearUsuario(RedirectAttributes redirectAttributes, Model model, Authentication authentication, @Valid Usuario usuario, BindingResult result) {
        User sessionUser = (User) authentication.getPrincipal();

        if (result.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("user", sessionUser);
            return "administradores/nuevo-usuario";
        }
        usuarioService.save(usuario);
        redirectAttributes.addFlashAttribute("success", "El usuario ha sido creado correctamente.");
        return "redirect:/administrador/inicio";
    }

    @GetMapping("/usuario/eliminar")
    public String eliminarUsuario(Model model, Authentication authentication){
        User sessionUser = (User) authentication.getPrincipal();

        model.addAttribute("usuarios", usuarioService.getAll());
        model.addAttribute("user", sessionUser);
        model.addAttribute("borrar", 1);
        return "administradores/nuevo-usuario";
    }

    @PostMapping("/usuarios/eliminar")
    public String eliminarUsuario(RedirectAttributes redirectAttributes, @RequestParam("nombreUsuario") String nombreUsuario) {
        Usuario usuario = usuarioService.findByUserName(nombreUsuario); //consigo el usuario a traves de su nombre
        if (usuario != null) {
            long totalUsuarios = usuarioService.countUsuarios();
            if (totalUsuarios > 1) {
                usuarioService.delete(usuario.getId()); //obtengo el id del usuario y lo borro de la db
                redirectAttributes.addFlashAttribute("success", "El usuario fue borrado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Error, no se puede borrar al unico usuario de la base de datos.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Error, no se encontro un usuario con ese nombre.");
        }
        return "redirect:/administrador/inicio";
    }
}