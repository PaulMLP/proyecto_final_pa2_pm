package com.uce.edu.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uce.edu.demo.repository.IVehiculoRepository;
import com.uce.edu.demo.repository.modelo.Vehiculo;
import com.uce.edu.demo.repository.modelo.VehiculoCampo;
import com.uce.edu.demo.repository.modelo.VehiculoLigero;
import com.uce.edu.demo.repository.modelo.VehiculoVipReporte;

@Service
public class VehiculoServiceImpl implements IVehiculoService {

	@Autowired
	private IVehiculoRepository vehiculoRepository;

	@Override
	public List<VehiculoCampo> buscarCampos(String campo) {
		// TODO Auto-generated method stub
		return this.vehiculoRepository.buscarCampos(campo);
	}

	@Override
	public Vehiculo buscarPorPlaca(String placa) {
		return this.vehiculoRepository.buscarPorPlaca(placa);
	}

	@Override
	public List<Vehiculo> buscar(String marca, String modelo) {
		// TODO Auto-generated method stub
		return this.buscar(marca, modelo);
	}

	@Override
	public int insertar(Vehiculo vehiculo) {
		// TODO Auto-generated method stub
		return this.vehiculoRepository.insertar(vehiculo);
	}

	@Override
	public List<Vehiculo> buscarMarca(String marca) {
		// TODO Auto-generated method stub
		return this.vehiculoRepository.buscarMarca(marca);
	}

	@Override
	@Transactional(value = TxType.MANDATORY)
	public void actualizar(Vehiculo vehiculo) {
		// TODO Auto-generated method stub
		this.vehiculoRepository.actualizar(vehiculo);
	}

	@Override
	public Vehiculo buscar(Integer id) {
		// TODO Auto-generated method stub
		return this.vehiculoRepository.buscar(id);
	}

	@Override
	public void eliminar(Integer id) {
		// TODO Auto-generated method stub
		this.vehiculoRepository.eliminar(id);
	}

	@Override
	public List<VehiculoVipReporte> buscarVehiculosVip(String fechaCadena) {
		// TODO Auto-generated method stub
		String[] fechaArray = fechaCadena.split("-");
		LocalDateTime fecha = LocalDateTime.of(Integer.parseInt(fechaArray[0]), Integer.parseInt(fechaArray[1]),1,0,0);
		System.out.println(fecha.toString());
		LocalDateTime fechaAux = fecha.plusMonths(1);
		List<VehiculoVipReporte> listaVehiculosVIP = this.vehiculoRepository.buscarVehiculosVip(fecha, fechaAux);

		List<VehiculoVipReporte> listaAgrupada = listaVehiculosVIP.stream()
				.collect(Collectors.groupingBy(VehiculoVipReporte::getPlaca)).entrySet().parallelStream()
				.map(setVehiculos -> setVehiculos.getValue().parallelStream()
						.reduce((vehiculoVIP, vehiculoAux) -> new VehiculoVipReporte(vehiculoVIP.getPlaca(),
								vehiculoVIP.getModelo(), vehiculoVIP.getMarca(),
								vehiculoVIP.getValorSubTotal().add(vehiculoAux.getValorSubTotal()),
								vehiculoVIP.getValorTotal().add(vehiculoAux.getValorTotal()))))
				.map(setVehiculos -> setVehiculos.get())
				.sorted(Comparator.comparing(VehiculoVipReporte::getValorTotal).reversed())
				.collect(Collectors.toList());

		return listaAgrupada;
	}

	@Override
	@Transactional(value = TxType.REQUIRED)
	public void ingresarVehiculo(String placa, String modelo, String marca, Integer anioFabricacion,
			String paisFabricacion, String cilindraje, BigDecimal avaluo, BigDecimal valorPorDia) {
		// TODO Auto-generated method stub
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPlaca(placa);
		vehiculo.setModelo(modelo);
		vehiculo.setMarca(marca);
		vehiculo.setAnioFabricacion(paisFabricacion);
		vehiculo.setPaisFabricacion(paisFabricacion);
		vehiculo.setCilindraje(cilindraje);
		vehiculo.setAvaluo(avaluo);
		vehiculo.setValorDia(valorPorDia);

		this.vehiculoRepository.insertar(vehiculo);

	}
	
	@Override
	public List<VehiculoLigero> buscarLigero(String marca, String modelo) {
		List<VehiculoLigero> lista = this.vehiculoRepository.buscarLigero(marca, modelo);
		List<VehiculoLigero> listaCambiada = lista.parallelStream().map(vehiculo -> {
			vehiculo.setValorCadena("$"+vehiculo.getValorDia());
			if(vehiculo.getEstado().equalsIgnoreCase("D")) vehiculo.setEstado("Disponible");
			else vehiculo.setEstado("No Disponible");
			return vehiculo;}).collect(Collectors.toList());
		
		return listaCambiada;
	}

}
