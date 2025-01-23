export interface Antiquity {
    idAntiquity: number; // Identifiant unique
    priceAntiquity: number; // Prix de l'antiquité
    descriptionAntiquity: string; // Description de l'antiquité
    titleAntiquity: string; // Titre de l'antiquité
    mailSeller: string ; // Email du membre associé 
    state: number; // État (1 = Neuf, 2 = Usagé)
    isDisplay: boolean | null; // Statut d'affichage (peut être null)
    mailAntiquarian: string | null; // Email de l'antiquaire (peut être null)
    photos: String[];
}