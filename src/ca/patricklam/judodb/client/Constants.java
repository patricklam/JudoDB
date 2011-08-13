package ca.patricklam.judodb.client;

public class Constants {
	public static int CURRENT_YEAR = 2012;
	
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
}
