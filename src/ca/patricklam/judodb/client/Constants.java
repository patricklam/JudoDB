package ca.patricklam.judodb.client;

public class Constants {	
	public static int currentSessionNo() { return 4; }
	public static Session currentSession() { 
		return session(currentSessionNo()); 
	}
	
	public static double PASSEPORT_JUDO_QC = 5.0;
	public static double NON_ANJOU = 5.0;
	
	static class Session {
		final int seqno;
		final String abbrev;
		final int effective_year;
		
		public Session(int seqno, String abbrev, int effective_year) {
			this.seqno = seqno; this.abbrev = abbrev;
			this.effective_year = effective_year;
		}
	}
	
	static class Categorie {
		final String name;
		final String abbrev;
		final int years_ago;
		final boolean noire;
		public Categorie(String name, String abbrev, int years_ago, boolean noire) { 
			this.name = name; this.abbrev = abbrev; 
			this.years_ago = years_ago; this.noire = noire;
		}
	}

	static class Cours {
		final String seqno;
		final String name;
		final String short_desc;
		final String entraineur;
		
		public Cours(String seqno, String name, String short_desc, String entraineur) {
			this.seqno = seqno;
			this.name = name; this.short_desc = short_desc;
			this.entraineur = entraineur;
		}
	}
	
	static class Escompte {
		final String seqno;
		final String name;
		final int amount; // in percent
		
		public Escompte(String seqno, String name, int amount) {
			this.seqno = seqno; this.name = name; this.amount = amount;
		}
	}
	
	public static final int escompte(String seqno) {
		for (Escompte e : ESCOMPTES) 
			if (e.seqno.equals(seqno))
				return e.amount;
		return 0;
	}
	
	static class CategorieSession {
		final int session_seqno_min, session_seqno_max;
		final String categorie_abbrev;
		final double frais_1_session, frais_2_session;
		final double frais_judo_qc;
		
		public CategorieSession(int session_seqno_min, int session_seqno_max, String categorie_abbrev,
				double frais_1_session, double frais_2_session, 
				double frais_judo_qc) {
			this.session_seqno_min = session_seqno_min;
			this.session_seqno_max = session_seqno_max;
			this.categorie_abbrev = categorie_abbrev;
			this.frais_1_session = frais_1_session;
			this.frais_2_session = frais_2_session;
			this.frais_judo_qc = frais_judo_qc;
		}
	}
	
	public static final Session[] SESSIONS = new Session[] {
		new Session(0, "A09", 2010),
		new Session(1, "H10", 2010),
		new Session(2, "A10", 2011),
		new Session(3, "H11", 2011),
		new Session(4, "A11", 2012),
		new Session(5, "H12", 2012)
	};
	
	public static Session session(int seqno) {
		for (Session s : SESSIONS)
			if (s.seqno == seqno)
				return s;
		return null;
	}
	
	public static String getCurrentSessionIds(int sessions) {
		if (sessions == 1) return currentSession().abbrev;
		if (sessions == 2) return currentSession().abbrev + " " + session(currentSessionNo()+1).abbrev;
		return "";
	}
				
	public static final Categorie[] CATEGORIES = new Categorie[] {
		new Categorie("Mini-Poussin", "U7", 7, false),
		new Categorie("Poussin", "U9", 9, false),
		new Categorie("Benjamin", "U11", 11, false),
		new Categorie("Minime", "U13", 13, false),
		new Categorie("Juvénile", "U15", 15, false),
		new Categorie("Cadet", "U17", 17, false),
		new Categorie("Junior", "U20", 20, false),
		new Categorie("Senior", "S", 0, false),
		new Categorie("Cadet Noire", "U17N", 17, true),
		new Categorie("Junior Noire", "U20N", 20, true),
		new Categorie("Senior Noire", "SN", 0, true),
	};


	public static final Cours[] COURS = new Cours[] {
		new Cours("0", "Adultes (LM2015-2145, V2000-2145)", "LM2015 V2000", "Rejean Lavoie"),
		new Cours("1", "Équipe compétition (LM1830-2015, V2000-2145)", "LM1830 V2000", "Luc Phan"),
		new Cours("2", "Intérmediares 7-12 ans (L1730-1830, V1830-2000)", "L1730 V1830", "Rejean Lavoie"),
		new Cours("3", "Débutants 7-12 ans (MaJ1730-1830)", "MaJ1730", "Luc Phan"),
		new Cours("4", "Débutants 5-6 ans (S900-1000)", "S900", "Lan-Anh Phan"),
		new Cours("5", "Anciens 5-6 ans (S1000-1100)", "S1000", "Lan-Anh Phan"),
		new Cours("6", "Anciens 5-6 ans (S1100-1200)", "S1100", "Alexandre Despres"),
		new Cours("7", "Anciens 7-9 ans (S1200-1330)", "S1200", "Alexandre Despres"),
		new Cours("8", "Anciens 7-9 ans (S1330-1500)", "S1330", "Alexandre Despres"),
		new Cours("9", "Débutants 7-11 ans (S1500-1630)", "S1500", "Alexandre Despres"),
		new Cours("10", "Débutants 5-6 ans (D930-1030)", "D930", "Lan-Anh Phan"),
		new Cours("11", "Débutants 7-10 ans (D1030-1230)", "D1030", "Lan-Anh Phan"),
		new Cours("12", "Débutants 7-11 ans (MV1730-1830)", "MV1730", "Luc Phan")
	};

	public static final CategorieSession[] CATEGORIES_SESSIONS = new CategorieSession[] {
		new CategorieSession(0, 1, "U7",  100.0, 175.0, 10.0),
		new CategorieSession(0, 1, "U9",  100.0, 175.0, 15.0),
		new CategorieSession(0, 1, "U11", 115.0, 195.0, 25.0),
		new CategorieSession(0, 1, "U13", 150.0, 225.0, 35.0),
		new CategorieSession(0, 1, "U15", 185.0, 290.0, 45.0),
		new CategorieSession(0, 1, "U17", 185.0, 290.0, 50.0),
		new CategorieSession(0, 1, "U20", 185.0, 300.0, 60.0),
		new CategorieSession(0, 1, "S",   210.0, 345.0, 65.0),
		new CategorieSession(0, 1, "U17N", 140.0, 140.0, 90.0),
		new CategorieSession(0, 1, "U20N", 140.0, 140.0, 90.0),
		new CategorieSession(0, 1, "SN", 140.0, 140.0, 100.0),
		new CategorieSession(2, 3, "U7", 109.0, 174.0, 11.0),
		new CategorieSession(2, 3, "U9", 109.0, 179.0, 16.0),
		new CategorieSession(2, 3, "U11", 129.0, 199.0, 26.0),
		new CategorieSession(2, 3, "U13", 159.0, 234.0, 36.0),
		new CategorieSession(2, 3, "U15", 189.0, 289.0, 46.0),
		new CategorieSession(2, 3, "U17", 194.0, 299.0, 51.0),
		new CategorieSession(2, 3, "U20", 199.0, 309.0, 61.0),
		new CategorieSession(2, 3, "S", 214.0, 344.0, 61.0),
		new CategorieSession(2, 3, "U17N", 145.0, 145.0, 90.0),
		new CategorieSession(2, 3, "U20N", 145.0, 145.0, 90.0),
		new CategorieSession(2, 3, "SN", 145.0, 145.0, 100.0),
		new CategorieSession(4, 5, "U7", 118.30, 182.0, 18.0),
		new CategorieSession(4, 5, "U9", 121.55, 187.0, 23.0),
		new CategorieSession(4, 5, "U11", 134.55, 207.0, 33.0),
		new CategorieSession(4, 5, "U13", 157.30, 242.0, 43.0),
		new CategorieSession(4, 5, "U15", 193.05, 297.0, 58.0),
		new CategorieSession(4, 5, "U17", 199.55, 307.0, 63.0),
		new CategorieSession(4, 5, "U20", 206.05, 317.0, 73.0),
		new CategorieSession(4, 5, "S", 228.80, 352.0, 78.0),
		new CategorieSession(4, 5, "U17N", 167.00, 172.0, 73.0),
		new CategorieSession(4, 5, "U20N", 167.00, 172.0, 73.0),
		new CategorieSession(4, 5, "SN", 167.0, 172.0, 83.0),
	};
	
	public static final double getFraisCours(int session, Categorie c, int sessionCount) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno_min <= session && 
					session <= cs.session_seqno_max && 
					cs.categorie_abbrev.equals(c.abbrev)) {
				if (sessionCount == 2)
					return cs.frais_2_session;
				return cs.frais_1_session;
			}
		return 0.0;
	}

	public static final double getFrais2Session(int session, Categorie c) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno_min <= session && 
					session <= cs.session_seqno_max && 
					cs.categorie_abbrev.equals(c.abbrev)) 
				return cs.frais_2_session;
		
		return 0.0;
	}
	
	public static final double getFraisJudoQC(int session, Categorie c) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno_min <= session && 
					session <= cs.session_seqno_max && 
					cs.categorie_abbrev.equals(c.abbrev)) 
				return cs.frais_judo_qc;
		
		return 0.0;
	}

	public static final Escompte[] ESCOMPTES = new Escompte[] {
		new Escompte("0", "Aucun", 0),
		new Escompte("1", "2e membre", 10),
		new Escompte("2", "3e membre", 15),
		new Escompte("3", "4e membre", 20),
		new Escompte("4", "Membre du CA", 50),
		new Escompte("5", "Cas spécial", -1)
	};
}