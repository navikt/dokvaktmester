package no.nav.dokvaktmester.api.dokarkiv;

import static java.lang.String.format;

/**
 * Enum for codes in T_K_UTSENDINGS_KANAL.
 *
 */
public enum UtsendingsKanalCode {

	/**
	 * EESSI
	 */
	EESSI,
	/**
	 * ALTINN
	 */
	ALTINN,
	/**
	 * Ditt NAV
	 */
	NAV_NO,
	/**
	 * Sentral print
	 */
	S,
	/**
	 * Lokal print
	 */
	L,
	/**
	 * Sikker digital post
	 */
	SDP,
	/**
	 * EIA
	 */
	EIA,
	/**
	 * Helsenettet
	 */
	HELSENETTET,
	/**
	 * Trygderetten
	 */
	TRYGDERETTEN,
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON,
	/**
	 * Midertidelig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140
	 **/
	MIGRERING_S,
	/**
	 * Midlertidig felt for migrering fra ondemand til dokarkiv, referanse sak: 5140
	 **/
	MIGRERING_L,
	/**
	 * Innlogget samtale
	 */
	NAV_NO_CHAT,
	/**
	 * Presentert direkte på nav.no for innlogget bruker
	 */
	NAV_NO_UTEN_VARSLING,
	/**
	 * Taushetsbelagt Post via Altinn
	 */
	DPVT,
	/**
	 * Digital Post Offentlig
	 */
	DPO;

	public static UtsendingsKanalCode fromString(String utsendingskanal) {
		return UtsendingsKanalCode.valueOf(utsendingskanal.toUpperCase());
	}

}
