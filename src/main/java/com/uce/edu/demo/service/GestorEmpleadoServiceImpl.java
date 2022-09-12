package com.uce.edu.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uce.edu.demo.repository.IClienteRepository;
import com.uce.edu.demo.repository.modelo.Cliente;
import com.uce.edu.demo.repository.modelo.Reserva;
import com.uce.edu.demo.repository.modelo.Vehiculo;

@Service
public class GestorEmpleadoServiceImpl implements IGestorEmpleadoService {

	@Autowired
	private IVehiculoService iVehiculoService;

	@Autowired
	private IReservaService iReservaService;

	@Autowired
	private IClienteGestorService clienteGestorService;

	@Autowired
	private IClienteRepository clienteRepository;

	@Override
	public void insertarCliente(Cliente cliente) {
		cliente.setRegistro("E");
		this.clienteRepository.insertar(cliente);
	}

	@Override
	public List<Vehiculo> verPorMarca(String marca) {
		// TODO Auto-generated method stub
		List<Vehiculo> vehiculo = this.iVehiculoService.buscarMarca(marca);
		return vehiculo;

	}

	// 2.e Retirar Vehiculo Reservado
	@Override
	@Transactional(value = TxType.REQUIRES_NEW)
	public String retirarVehiculoReservado(String numeroReserva) {
		Reserva reserva = this.iReservaService.buscarPorNumero(numeroReserva);
		if (reserva.getEstado().equals("E")) {
			return "Ya ha sido retirado";
		} else {
			reserva.setEstado("E");
			Vehiculo vehiculo = reserva.getVehiculo();
			vehiculo.setEstado("ND");

			this.iVehiculoService.actualizar(vehiculo);
			this.iReservaService.actualizarReserva(reserva);
			return "Se ha retirado con exito";
		}
	}

	// 2.f Retirar Vehiculo sin Reserva
	@Override
	@Transactional(value = TxType.REQUIRES_NEW)
	public String retirarVehiculoSinReserva(String placa, String cedula, LocalDateTime fechaInicio,
			LocalDateTime fechaFin, String tarjeta) {
		Reserva r = this.clienteGestorService.disponibilidad(placa, cedula, fechaInicio, fechaFin);
		if (r != null) {
			this.clienteGestorService.registrarReserva(r, tarjeta);
			return this.retirarVehiculoReservado(r.getNumero());
		} else {
			return "no disponible";
		}
	}

}
