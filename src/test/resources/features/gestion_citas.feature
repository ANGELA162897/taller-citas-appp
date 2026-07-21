Feature: Gestion de citas del taller mecanico

  1. Agendar un cambio de aceite de forma exitosa
  #    (la cita queda PROGRAMADA y se notifica el agendamiento)
        Given que el reloj del servicio esta fijado un dia antes del DIA a las 08:00
        And existe un mecanico con id 1 de especialidad CAMBIO_ACEITE
        When intento agendar un CAMBIO_ACEITE con el mecanico 1 para la placa "ABA-319" el DIA a las 10:00
        Then la cita debe quedar programada y notificada correctamente
  #
  # 2. Rechazar una reparacion de motor en la tarde
  #    (los servicios pesados solo se atienden entre las 08:00 y las 12:00)
  #    Given que el reloj del servicio esta fijado un dia antes del DIA a las 08:00
       And existe un mecanico con id 2 de especialidad REPARACION_MOTOR
       When intento agendar una REPARACION_MOTOR con el mecanico 2 para la placa "ABA-319" el DIA a las 15:00
       Then el agendamiento debe ser rechazado por horario no permitido

  # 3. Cancelar con penalidad por aviso tardio
  #    (cancelar con menos de 24 horas aplica una penalidad de 50.00)
  #     Given que el reloj del servicio esta fijado para el DIA a las 08:00
        And existe una cita programada con id 10 para el DIA a las 10:00
        When solicito cancelar la cita con id 10
        Then la cancelacion debe ser exitosa con una penalidad de 50.00 y notificada

  # 4. Rechazar un agendamiento por horario ocupado
  #    (el mecanico ya tiene una cita programada que se superpone)
        Given que el reloj del servicio esta fijado un dia antes del DIA a las 08:00
        And el mecanico 1 tiene una cita programada el DIA de 10:00 a 12:00
        When intento agendar un CAMBIO_ACEITE con el mecanico 1 para la placa "ABA-319" el DIA a las 11:00
        Then el agendamiento debe ser rechazado por horario ocupado
