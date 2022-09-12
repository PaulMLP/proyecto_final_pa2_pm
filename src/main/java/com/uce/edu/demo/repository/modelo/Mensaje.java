package com.uce.edu.demo.repository.modelo;

public class Mensaje {
	private String contenido;
	private String tipo;

	public Mensaje() {
		// TODO Auto-generated constructor stub
	}

	public Mensaje(String contenido, String tipo) {
		super();
		this.contenido = contenido;
		this.tipo = tipo;
	}

	@Override
	public String toString() {
		return "Mensaje [contenido=" + contenido + ", tipo=" + tipo + "]";
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

}