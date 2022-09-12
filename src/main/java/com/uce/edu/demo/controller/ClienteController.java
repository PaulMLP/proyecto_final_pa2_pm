package com.uce.edu.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uce.edu.demo.repository.modelo.Cliente;
import com.uce.edu.demo.repository.modelo.Mensaje;
import com.uce.edu.demo.repository.modelo.Reserva;
import com.uce.edu.demo.repository.modelo.VehiculoCampo;
import com.uce.edu.demo.repository.modelo.VehiculoLigero;
import com.uce.edu.demo.service.ClienteGestorServiceImpl;
import com.uce.edu.demo.service.IClienteService;
import com.uce.edu.demo.service.IVehiculoService;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

	@Autowired
	private ClienteGestorServiceImpl clienteGestorServiceImpl;

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IVehiculoService vehiculoService;

	private Reserva reserva;
	private Cliente cliente;

	@GetMapping("/home")
	public String principal(@ModelAttribute Mensaje mensaje, Model modelo) {
		List<VehiculoCampo> vehiculosMarca = this.vehiculoService.buscarCampos("marca");
		List<VehiculoCampo> vehiculosModelo = this.vehiculoService.buscarCampos("modelo");
		modelo.addAttribute("mensaje", mensaje);
		modelo.addAttribute("vehiculosMarca", vehiculosMarca);
		modelo.addAttribute("vehiculosModelo", vehiculosModelo);
		return "home";
	}

	@GetMapping("/volver")
	public String redireccionHome(Model modelo) {
		return "redirect:/clientes/home";
	}

	@GetMapping("/registra/o/actualiza")
	public String registrarCliente(@RequestParam("cedula") String cedula, Model modelo) {
		Cliente cliente = this.clienteService.buscarClientePorCedula(cedula);
		if (cliente == null) {
			Cliente c = new Cliente();
			c.setCedula(cedula);
			modelo.addAttribute("cliente", c);
			return "registro";
		} else {
			this.cliente = cliente;
			System.out.println("********" + cliente);
			modelo.addAttribute("cliente", this.cliente);
			return "actualizar";
		}
	}

	@GetMapping("/actualizarCliente")
	public String actualizarCliente(Cliente cliente) {
		cliente.setId(this.cliente.getId());
		cliente.setCedula(this.cliente.getCedula());
		cliente.setRegistro(this.cliente.getRegistro());
		System.out.println("********" + cliente);
		this.clienteService.actualizar(cliente);
		return "redirect:/clientes/home";
	}

	@PostMapping("/registrarCliente")
	public String insertarCliente(Cliente cliente) {
		this.clienteService.insertar(cliente);
		return "redirect:/clientes/home";
	}

	@GetMapping("/buscar/vehiculos")
	public String resultados(@ModelAttribute("mensaje") Mensaje mensaje, @RequestParam("marca") String marca,
			@RequestParam("modelo") String modelo, Model model, RedirectAttributes redirectAttrs) {
		List<VehiculoLigero> lista = this.vehiculoService.buscarLigero(marca, modelo);
		if (lista.size() != 0) {
			model.addAttribute("vehiculos", lista);
			return "vehiculos";
		}
		redirectAttrs.addFlashAttribute("mensaje",
				new Mensaje("No existen vehículos con dichas características", "Error"));
		model.addAttribute("vehiculos", lista);
		return "redirect:/clientes/home";
	}

	@GetMapping("/buscar/reserva")
	public String buscarReserva(@ModelAttribute("mensaje") Mensaje mensaje, Model modelo) {
		modelo.addAttribute("mensaje", mensaje);
		return "reserva";
	}

	@GetMapping("/buscar/disponible")
	public String buscarDisponible(@ModelAttribute("mensaje") Mensaje mensaje, @RequestParam("placa") String placa,
			@RequestParam("cedula") String cedula,
			@RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
			@RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
			Model modelo, RedirectAttributes redirectAttrs) {
		// verificar que exista el cliente y el vehiculo
		if (this.clienteGestorServiceImpl.datosValidos(placa, cedula)) {
			Reserva reserva = this.clienteGestorServiceImpl.disponibilidad(placa, cedula, fechaInicio, fechaFin);
			if (reserva != null) {
				modelo.addAttribute("reserva", reserva);
				this.reserva = reserva;
				return "cobro";
			} else {
				String fechaDisponible = this.clienteGestorServiceImpl
						.fechaDisponible(this.vehiculoService.buscarPorPlaca(placa), fechaInicio, fechaFin);
				redirectAttrs.addFlashAttribute("mensaje",
						new Mensaje("El vehículo ya tiene reserva en las fechas selccionadas!!!\n"
								+ "Reservación disponible a partir de: " + fechaDisponible, "Error"));
				modelo.addAttribute("reserva", null);
				return "redirect:/clientes/buscar/reserva";
			}

		} // si no existe el cliente o el vehiculo
		else {
			redirectAttrs.addFlashAttribute("mensaje", new Mensaje("Cliente o Vehiculo erróneo!!", "Error"));
			modelo.addAttribute("reserva", null);
			return "redirect:/clientes/buscar/reserva";
		}

	}

	@PostMapping("/cobro/reserva")
	public String realizarReserva(@RequestParam("tarjeta") String tarjeta) {
		this.clienteGestorServiceImpl.registrarReserva(this.reserva, tarjeta);
		return "redirect:/clientes/home";
	}

}