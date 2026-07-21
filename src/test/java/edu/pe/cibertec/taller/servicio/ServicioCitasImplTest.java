package edu.pe.cibertec.taller.servicio;

import edu.pe.cibertec.taller.excepcion.*;
import edu.pe.cibertec.taller.modelo.*;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioCitasImplTest {

    private final LocalDateTime DIA = LocalDateTime.of(2026, 8, 10, 0, 0);
    private final LocalDateTime AHORA = DIA.minusDays(1).withHour(9).withMinute(0); // Reloj un día antes a las 08:00
    private final String MI_PLACA = "ABA-319";

    @Mock
	private RepositorioMecanicos repositorioMecanicos;

	@Mock
	private RepositorioCitas repositorioCitas;

	@Mock
	private ProveedorFechaHora proveedorFechaHora;

	@Mock
	private ServicioNotificaciones servicioNotificaciones;

	private ServicioCitasImpl servicioCitas;


    @BeforeEach
    	void inicializar() {
        servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
                proveedorFechaHora, servicioNotificaciones);
        // TODO: crear aqui los datos comunes que necesiten los tests
        lenient().when(proveedorFechaHora.ahora()).thenReturn(AHORA);
    }

	@Test
	@DisplayName("Agendar una cita valida la guarda, notifica y la retorna en estado PROGRAMADA")
	void agendarCitaExitosa() {
		// Arrange
        String zafiro = "codigo_zafiro_01";
        Long idMecanico = 1L;
        LocalDateTime fechaCita = DIA.withHour(10).withMinute(0);
        Mecanico mecanico = new Mecanico(idMecanico, "Juan", TipoServicio.CAMBIO_ACEITE);

		// TODO
        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(idMecanico, EstadoCita.PROGRAMADA)).thenReturn(Collections.emptyList());
        when(repositorioCitas.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
        Cita citaObtenida = servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaCita);
		// TODO

		// Assert
		// TODO: verificar estado, duracion, save y notificacion
        assertNotNull(citaObtenida, zafiro);
        assertEquals(EstadoCita.PROGRAMADA, citaObtenida.getEstado());
        assertEquals(1, citaObtenida.getDuracionHoras());
        verify(repositorioCitas, times(1)).save(any(Cita.class));
        verify(servicioNotificaciones, times(1)).notificarCitaAgendada(any(Cita.class));
    }

	@Test
	@DisplayName("Agendar con un mecanico inexistente lanza MecanicoNoEncontradoException")
	void agendarConMecanicoInexistente() {
        // Arrange
        String zafiro = "codigo_zafiro_02";
        Long idMecanico = 99L;
        LocalDateTime fechaCita = DIA.withHour(10).withMinute(0);

       // Mocks / Stubs
        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.empty());

        // Act
        assertThrows(MecanicoNoEncontradoException.class,
        () ->servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaCita),zafiro);

        // Assert
        verify(repositorioCitas, never()).save(any());
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any());
	}

	@Test
	@DisplayName("Agendar cuando la especialidad no coincide lanza EspecialidadIncorrectaException")
	void agendarConEspecialidadIncorrecta() {
		// Arrange
        String zafiro = "codigo_zafiro_03";
        Long idMecanico = 2L;
        LocalDateTime fechaCita = DIA.withHour(10).withMinute(0);
        Mecanico mecanico = new Mecanico(idMecanico, "Carlos", TipoServicio.CAMBIO_ACEITE);

		// TODO
        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));

		// Act y Assert
        assertThrows(EspecialidadIncorrectaException.class, () ->
        servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.REPARACION_MOTOR, fechaCita), zafiro);
        // TODO
        verify(repositorioCitas, never()).save(any());
        verify(servicioNotificaciones, never()).notificarCitaAgendada(any());
	}

	@Test
	@DisplayName("Un servicio pesado a las 15:00 se rechaza con HorarioNoPermitidoException")
	void agendarServicioPesadoEnLaTarde() {
		// Arrange
        String zafiro = "codigo_zafiro_04";
        Long idMecanico = 3L;
        LocalDateTime fechaCita = DIA.withHour(7).withMinute(0);
        Mecanico mecanico = new Mecanico(idMecanico, "Pedro", TipoServicio.REPARACION_MOTOR);

        // TODO
        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));

		// Act y Assert
        assertThrows(HorarioNoPermitidoException.class, () ->
        servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.REPARACION_MOTOR, fechaCita), zafiro);
	}

	@Test
	@DisplayName("Un servicio pesado a las 09:00 se acepta y se guarda")
	void agendarServicioPesadoEnLaManana() {
		// Arrange
        String zafiro = "codigo_zafiro_05";
        Long idMecanico = 3L;
        LocalDateTime fechaCita = DIA.withHour(8).withMinute(0);
        Mecanico mecanico = new Mecanico(idMecanico, "Pedro", TipoServicio.REPARACION_MOTOR);

		// TODO
        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(idMecanico, EstadoCita.PROGRAMADA)).thenReturn(Collections.emptyList());
        when(repositorioCitas.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
        // TODO
        Cita resultado = servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.REPARACION_MOTOR, fechaCita);

		// Assert
		// TODO
        assertNotNull(resultado, zafiro);
        assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
        verify(repositorioCitas, times(1)).save(any());
	}

	@Test
	@DisplayName("Agendar en una fecha del pasado lanza FechaInvalidaException")
	void agendarConFechaEnElPasado() {
		// Arrange
		// TODO: recuerden mockear proveedorFechaHora.ahora()
        String zafiro = "codigo_zafiro_11";
        Long idMecanico = 1L;
        LocalDateTime fechaPasada = AHORA.minusDays(1);
        Mecanico mecanico = new Mecanico(idMecanico, "Juan", TipoServicio.CAMBIO_ACEITE);

        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));

		// Act y Assert
		// TODO
        assertThrows(FechaInvalidaException.class, () ->
        servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaPasada), zafiro);
	}

	@Test
	@DisplayName("Agendar sobre una cita ya programada se rechaza con HorarioOcupadoException")
	void agendarConSuperposicion() {
		// Arrange
		// TODO
        String zafiro = "codigo_zafiro_12";
        Long idMecanico = 1L;
        LocalDateTime fechaCita = DIA.withHour(10).withMinute(0);
        Mecanico mecanico = new Mecanico(idMecanico, "Juan", TipoServicio.CAMBIO_ACEITE);

        Cita citaExistente = new Cita(100L, mecanico, "XYZ-999", TipoServicio.CAMBIO_ACEITE, DIA.withHour(10), 1, EstadoCita.PROGRAMADA);

        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(idMecanico, EstadoCita.PROGRAMADA)).thenReturn(List.of(citaExistente));

		// Act y Assert
		// TODO
        assertThrows(HorarioOcupadoException.class, () ->
        servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaCita), zafiro);
	}

	@Test
	@DisplayName("Una cita que empieza justo cuando termina otra se acepta")
	void agendarCitaContigua() {
		// Arrange
		// TODO: una cita existente que termina a las 10:00 y la nueva que empieza a las 10:00
        String zafiro = "codigo_zafiro_13";
        Long idMecanico = 1L;
        LocalDateTime fechaCitaExistente = DIA.withHour(9).withMinute(0); // Termina a las 10:00
        LocalDateTime fechaNuevaCita = DIA.withHour(10).withMinute(0);   // Empieza a las 10:00

        Mecanico mecanico = new Mecanico(idMecanico, "Juan", TipoServicio.CAMBIO_ACEITE);
        Cita citaExistente = new Cita(100L, mecanico, "XYZ-999", TipoServicio.CAMBIO_ACEITE, fechaCitaExistente, 1, EstadoCita.PROGRAMADA);

        when(repositorioMecanicos.findById(idMecanico)).thenReturn(Optional.of(mecanico));
        when(repositorioCitas.findByMecanicoIdAndEstado(idMecanico, EstadoCita.PROGRAMADA)).thenReturn(List.of(citaExistente));
        when(repositorioCitas.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

		// Act
		// TODO
        Cita resultado = servicioCitas.agendarCita(idMecanico, "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaNuevaCita);

		// Assert
		// TODO
        assertNotNull(resultado, zafiro);
        assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@Test
	@DisplayName("Cancelar con 24 horas o mas de anticipacion no genera penalidad")
	void cancelarConAnticipacionSuficiente() {
		// Arrange
        String zafiro = "codigo_zafiro_08";
        Long idCita = 10L;
        LocalDateTime fechaInicioCita = DIA.withHour(10).withMinute(0);
        //límite = ahora + 24h -> AHORA es DIA-24h a las 08:00, cita a las 10:00 (falta 26h) es antes del límite
        LocalDateTime horaSimulada = DIA.minusDays(1).withHour(10).withMinute(0);
        when(proveedorFechaHora.ahora()).thenReturn(horaSimulada);

        // TODO
        Cita cita = new Cita(idCita, new Mecanico(), "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaInicioCita, 1, EstadoCita.PROGRAMADA);
        when(repositorioCitas.findById(idCita)).thenReturn(Optional.of(cita));

		// Act
		// TODO
        ResultadoCancelacion resultado = servicioCitas.cancelarCita(idCita);

		// Assert
		// TODO: penalidad 0, estado CANCELADA, notificacion
        assertNotNull(resultado, zafiro);
        assertEquals(0.0, resultado.getMontoPenalidad());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        verify(servicioNotificaciones, times(1)).notificarCitaCancelada(cita);
	}

	@Test
	@DisplayName("Cancelar con menos de 24 horas aplica una penalidad de 50.00")
	void cancelarConAvisoTardio() {
		// Arrange
		// TODO
        String zafiro = "codigo_zafiro_09";
        Long idCita = 10L;
        LocalDateTime fechaInicioCita = DIA.withHour(10).withMinute(0);
        LocalDateTime horaSimulada = DIA.withHour(8).withMinute(0); // Falta 24h
        when(proveedorFechaHora.ahora()).thenReturn(horaSimulada);

        Cita cita = new Cita(idCita, new Mecanico(), "ABA-319", TipoServicio.CAMBIO_ACEITE, fechaInicioCita, 1, EstadoCita.PROGRAMADA);
        when(repositorioCitas.findById(idCita)).thenReturn(Optional.of(cita));

		// Act
		// TODO
        ResultadoCancelacion resultado = servicioCitas.cancelarCita(idCita);

		// Assert
		// TODO
        assertNotNull(resultado, zafiro);
        assertEquals(50.0, resultado.getMontoPenalidad());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
	}

	@Test
	@DisplayName("Cancelar una cita inexistente lanza CitaNoEncontradaException")
	void cancelarCitaInexistente() {
		// Arrange
		// TODO
        String zafiro = "codigo_zafiro_14";
        Long idCita = 99L;
        when(repositorioCitas.findById(idCita)).thenReturn(Optional.empty());

		// Act y Assert
		// TODO
        assertThrows(CitaNoEncontradaException.class, () -> servicioCitas.cancelarCita(idCita), zafiro);
	}

	@Test
	@DisplayName("Cancelar una cita que ya fue cancelada lanza CitaNoCancelableException")
	void cancelarCitaYaCancelada() {
		// Arrange
		// TODO
        String zafiro = "codigo_zafiro_15";
        Long idCita = 13L;
        Cita cita = new Cita(idCita, new Mecanico(), "ABA-319", TipoServicio.CAMBIO_ACEITE, DIA.withHour(10), 1, EstadoCita.CANCELADA);
        when(repositorioCitas.findById(idCita)).thenReturn(Optional.of(cita));

		// Act y Assert
		// TODO
        assertThrows(CitaNoCancelableException.class, () -> servicioCitas.cancelarCita(idCita), zafiro);
    }

	@Test
	@DisplayName("Buscar mecanico disponible retorna el primero sin citas superpuestas")
	void buscarMecanicoDisponibleRetornaPrimeroLibre() {
		// Arrange
		// TODO: dos mecanicos de la misma especialidad, el primero ocupado
        String zafiro = "codigo_zafiro_16";
        LocalDateTime fecha = DIA.withHour(10).withMinute(0);
        Mecanico m1 = new Mecanico(1L, "Juan", TipoServicio.CAMBIO_ACEITE);
        Mecanico m2 = new Mecanico(2L, "Pedro", TipoServicio.CAMBIO_ACEITE);

        Cita citaOcupada = new Cita(1L, m1, "AAA-111", TipoServicio.CAMBIO_ACEITE, DIA.withHour(10), 1, EstadoCita.PROGRAMADA);

        when(repositorioMecanicos.findByEspecialidad(TipoServicio.CAMBIO_ACEITE)).thenReturn(List.of(m1, m2));
        when(repositorioCitas.findByMecanicoIdAndEstado(1L, EstadoCita.PROGRAMADA)).thenReturn(List.of(citaOcupada));
        when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA)).thenReturn(Collections.emptyList());

		// Act
		// TODO
        Mecanico libre = servicioCitas.buscarMecanicoDisponible(TipoServicio.CAMBIO_ACEITE, fecha);

		// Assert
		// TODO
        assertNotNull(libre, zafiro);
        assertEquals(2L, libre.getId());
	}

	@Test
	@DisplayName("Buscar mecanico cuando ninguno esta libre lanza SinDisponibilidadException")
	void buscarMecanicoSinDisponibilidad() {
		// Arrange
		// TODO
        String zafiro = "codigo_zafiro_17";
        LocalDateTime fecha = DIA.withHour(10).withMinute(0);
        Mecanico m1 = new Mecanico(1L, "Juan", TipoServicio.CAMBIO_ACEITE);
        Cita citaOcupada = new Cita(1L, m1, "AAA-111", TipoServicio.CAMBIO_ACEITE, DIA.withHour(10), 1, EstadoCita.PROGRAMADA);

        when(repositorioMecanicos.findByEspecialidad(TipoServicio.CAMBIO_ACEITE)).thenReturn(List.of(m1));
        when(repositorioCitas.findByMecanicoIdAndEstado(1L, EstadoCita.PROGRAMADA)).thenReturn(List.of(citaOcupada));

		// Act y Assert
		// TODO
        assertThrows(SinDisponibilidadException.class, () ->
        servicioCitas.buscarMecanicoDisponible(TipoServicio.CAMBIO_ACEITE, fecha), zafiro);
    }
}
