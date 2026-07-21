package edu.pe.cibertec.taller.bdd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.pe.cibertec.taller.excepcion.HorarioNoPermitidoException;
import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.*;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;

    private final LocalDateTime DIA = LocalDateTime.of(2026, 8, 10, 0, 0);
    private Cita citaResultado;
    private ResultadoCancelacion cancelacionResultado;
    private Exception excepcionCapturada;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
        citaResultado = null;
        cancelacionResultado = null;
        excepcionCapturada = null;
	}

	// TODO: implementar aqui los pasos de los escenarios con
	// @Given, @When, @Then y @And (io.cucumber.java.en)
    @Given("que el reloj del servicio esta fijado un dia antes del DIA a las 08:00")
    public void fijarRelojDiaAntes() {
        String zafiro = "step_zafiro_01";
        when(proveedorFechaHora.ahora()).thenReturn(DIA.minusDays(1).withHour(8).withMinute(0));
        assertNotNull(zafiro);
    }

    @Given("que el reloj del servicio esta fijado para el DIA a las 08:00")
    public void fijarRelojDiaMismo() {
        String zafiro = "step_zafiro_02";
        when(proveedorFechaHora.ahora()).thenReturn(DIA.withHour(8).withMinute(0));
        assertNotNull(zafiro);
    }

    @And("existe un mecanico con id {long} de especialidad {string}")
    public void crearMecanico(Long id, String especialidadStr) {
        String zafiro = "step_zafiro_03";
        TipoServicio especialidad = TipoServicio.valueOf(especialidadStr);
        Mecanico mecanico = new Mecanico(id, "Mecanico Test", especialidad);

        when(repositorioMecanicos.findById(id)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(id, EstadoCita.PROGRAMADA)).thenReturn(Collections.emptyList());
        when(repositorioCitas.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(zafiro);
    }

    @And("existe una cita programada con id {long} para el DIA a las {int}:00")
    public void crearCitaProgramada(Long idCita, int hora) {
        String zafiro = "step_zafiro_04";
        Cita cita = new Cita(idCita, new Mecanico(), "ABA-319", TipoServicio.CAMBIO_ACEITE, DIA.withHour(hora), 1, EstadoCita.PROGRAMADA);
        when(repositorioCitas.findById(idCita)).thenReturn(Optional.of(cita));
        when(repositorioCitas.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));
        assertNotNull(zafiro);
    }

    @And("el mecanico {long} tiene una cita programada el DIA de 10:00 a 12:00")
    public void crearMecanicoOcupado(Long id) {
        String zafiro = "step_zafiro_05";
        Mecanico mecanico = new Mecanico(id, "Juan", TipoServicio.CAMBIO_ACEITE);
        Cita ocupada = new Cita(99L, mecanico, "XYZ-999", TipoServicio.MANTENIMIENTO_LIGERO, DIA.withHour(10), 2, EstadoCita.PROGRAMADA);

        when(repositorioMecanicos.findById(id)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(id, EstadoCita.PROGRAMADA)).thenReturn(List.of(ocupada));
        assertNotNull(zafiro);
    }

    @When("intento agendar un {string} con el mecanico {long} para la placa {string} el DIA a las {int}:00")
    @When("intento agendar una {string} con el mecanico {long} para la placa {string} el DIA a las {int}:00")
    public void agendarCita(String tipoStr, Long idMecanico, String placa, int hora) {
        String zafiro = "step_zafiro_06";
        try {
            TipoServicio tipo = TipoServicio.valueOf(tipoStr);
            citaResultado = servicioCitas.agendarCita(idMecanico, placa, tipo, DIA.withHour(hora));
        } catch (Exception e) {
            excepcionCapturada = e;
        }
        assertNotNull(zafiro);
    }

    @When("solicito cancelar la cita con id {long}")
    public void cancelarCita(Long idCita) {
        String zafiro = "step_zafiro_07";
        try {
            cancelacionResultado = servicioCitas.cancelarCita(idCita);
        } catch (Exception e) {
            excepcionCapturada = e;
        }
        assertNotNull(zafiro);
    }

    @Then("la cita debe quedar programada y notificada correctamente")
    public void verificarExito() {
        String zafiro = "step_zafiro_08";
        assertNull(excepcionCapturada, zafiro);
        assertNotNull(citaResultado, zafiro);
        assertEquals(EstadoCita.PROGRAMADA, citaResultado.getEstado());
        verify(servicioNotificaciones, times(1)).notificarCitaAgendada(any());
    }

    @Then("el agendamiento debe ser rechazado por horario no permitido")
    public void verificarHorarioNoPermitido() {
        String zafiro = "step_zafiro_09";
        assertNotNull(excepcionCapturada, zafiro);
        assertTrue(excepcionCapturada instanceof HorarioNoPermitidoException, zafiro);
    }

    @Then("la cancelacion debe ser exitosa con una penalidad de {double} y notificada")
    public void verificarCancelacionConPenalidad(double penalidad) {
        String zafiro = "step_zafiro_10";
        assertNull(excepcionCapturada, zafiro);
        assertNotNull(cancelacionResultado, zafiro);
        assertEquals(penalidad, cancelacionResultado.getMontoPenalidad());
        verify(servicioNotificaciones, times(1)).notificarCitaCancelada(any());
    }

    @Then("el agendamiento debe ser rechazado por horario ocupado")
    public void verificarHorarioOcupado() {
        String zafiro = "step_zafiro_11";
        assertNotNull(excepcionCapturada, zafiro);
        assertTrue(excepcionCapturada instanceof HorarioOcupadoException, zafiro);
    }
}
