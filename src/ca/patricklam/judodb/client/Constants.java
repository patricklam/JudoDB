package ca.patricklam.judodb.client;

public class Constants {
	public static int CURRENT_YEAR = 2012;
	public static String CURRENT_SESSION = "A11";
	public static int CURRENT_SESSION_SEQNO = 0;
	public static String NEXT_SESSION = "H12";
	public static double PASSEPORT_JUDO_QC = 5.0;
	public static double NON_ANJOU = 5.0;
	
	static class Categorie {
		String name;
		String abbrev;
		int years_ago;
		boolean noire;
		public Categorie(String name, String abbrev, int years_ago, boolean noire) { 
			this.name = name; this.abbrev = abbrev; 
			this.years_ago = years_ago; this.noire = noire;
		}
	}

	static class Cours {
		String seqno;
		String name;
		String short_desc;
		String entraineur;
		
		public Cours(String seqno, String name, String short_desc, String entraineur) {
			this.seqno = seqno;
			this.name = name; this.short_desc = short_desc;
			this.entraineur = entraineur;
		}
	}
	
	static class Escompte {
		String seqno;
		String name;
		int amount; // in percent
		
		public Escompte(String seqno, String name, int amount) {
			this.seqno = seqno; this.name = name; this.amount = amount;
		}
	}
	
	public static final int getEscompte(String seqno) {
		for (Escompte e : ESCOMPTES) 
			if (e.seqno.equals(seqno))
				return e.amount;
		return 0;
	}
	
	static class CategorieSession {
		int session_seqno;
		String categorie_abbrev;
		double frais_1_session, frais_2_session;
		double frais_judo_qc;
		
		public CategorieSession(int session_seqno, String categorie_abbrev,
				double frais_1_session, double frais_2_session, 
				double frais_judo_qc) {
			this.session_seqno = session_seqno;
			this.categorie_abbrev = categorie_abbrev;
			this.frais_1_session = frais_1_session;
			this.frais_2_session = frais_2_session;
			this.frais_judo_qc = frais_judo_qc;
		}
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
		new Cours("0", "Adultes (LM2015-2145, V2000-2145", "LM2015 V2000", "Rejean Lavoie"),
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
		new CategorieSession(0, "U7", 118.30, 182.0, 18.0),
		new CategorieSession(0, "U9", 121.55, 187.0, 23.0),
		new CategorieSession(0, "U11", 134.55, 207.0, 33.0),
		new CategorieSession(0, "U13", 157.30, 242.0, 43.0),
		new CategorieSession(0, "U15", 193.05, 297.0, 58.0),
		new CategorieSession(0, "U17", 199.55, 307.0, 63.0),
		new CategorieSession(0, "U20", 206.05, 317.0, 73.0),
		new CategorieSession(0, "S", 228.80, 352.0, 78.0),
		new CategorieSession(0, "U17N", 167.00, 172.0, 73.0),
		new CategorieSession(0, "U20N", 167.00, 172.0, 73.0),
		new CategorieSession(0, "SN", 167.0, 172.0, 83.0),
	};
	
	public static final double getFrais1Session(int session, Categorie c) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno == session && cs.categorie_abbrev.equals(c.abbrev)) 
				return cs.frais_1_session;
		
		return 0.0;
	}

	public static final double getFrais2Session(int session, Categorie c) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno == session && cs.categorie_abbrev.equals(c.abbrev)) 
				return cs.frais_2_session;
		
		return 0.0;
	}
	
	public static final double getFraisJudoQC(int session, Categorie c) {
		for (CategorieSession cs : CATEGORIES_SESSIONS)
			if (cs.session_seqno == session && cs.categorie_abbrev.equals(c.abbrev)) 
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