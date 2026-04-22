package com.ossanasur.cbconnect.historique;
import com.ossanasur.cbconnect.module.pays.repository.PaysRepository;
import com.ossanasur.cbconnect.module.sinistre.dto.request.VictimeRequest;
import com.ossanasur.cbconnect.module.sinistre.entity.Victime;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;
@Service @RequiredArgsConstructor
public class VictimeVersioningService extends AbstractVersioningService<Victime, VictimeRequest> {
    private final VictimeRepository repository;
    private final PaysRepository paysRepository;
    @Override protected JpaRepository<Victime,Integer> getRepository(){return repository;}
    @Override protected Victime findActiveByTrackingId(UUID id){return repository.findActiveByTrackingId(id).orElse(null);}
    @Override protected UUID getTrackingId(Victime e){return e.getVictimeTrackingId();}
    @Override protected Victime mapToEntity(VictimeRequest r, Victime ex){
        Victime u=cloneEntity(ex);
        if(r.nom()!=null)u.setNom(r.nom()); if(r.prenoms()!=null)u.setPrenoms(r.prenoms());
        if(r.dateNaissance()!=null)u.setDateNaissance(r.dateNaissance());
        if(r.sexe()!=null)u.setSexe(r.sexe()); if(r.nationalite()!=null)u.setNationalite(r.nationalite());
        if(r.statutActivite()!=null)u.setStatutActivite(r.statutActivite());
        if(r.revenuMensuel()!=null)u.setRevenuMensuel(r.revenuMensuel());
        if(r.paysResidenceTrackingId()!=null) paysRepository.findActiveByTrackingId(r.paysResidenceTrackingId()).ifPresent(u::setPaysResidence);

        // V22 / V26 — champs étendus (victime + adversaire, avec extensions engin)
        if(r.typeVictime()!=null)   u.setTypeVictime(r.typeVictime());
        if(r.estAdversaire()!=null) u.setEstAdversaire(r.estAdversaire());
        if(r.profession()!=null)    u.setProfession(r.profession());
        if(r.typeDommage()!=null)   u.setTypeDommage(r.typeDommage());
        if(r.telephone()!=null)     u.setTelephone(r.telephone());
        if(r.numeroPermis()!=null)  u.setNumeroPermis(r.numeroPermis());
        if(r.categoriesPermis()!=null) u.setCategoriesPermis(String.join(",", r.categoriesPermis()));
        if(r.dateDelivrance()!=null)u.setDateDelivrance(r.dateDelivrance());
        if(r.lieuDelivrance()!=null)u.setLieuDelivrance(r.lieuDelivrance());
        if(r.marqueVehicule()!=null)   u.setMarqueVehicule(r.marqueVehicule());
        if(r.modeleVehicule()!=null)   u.setModeleVehicule(r.modeleVehicule());
        if(r.genreVehicule()!=null)    u.setGenreVehicule(r.genreVehicule());
        if(r.couleurVehicule()!=null)  u.setCouleurVehicule(r.couleurVehicule());
        if(r.immatriculation()!=null)  u.setImmatriculation(r.immatriculation());
        if(r.numeroChassis()!=null)    u.setNumeroChassis(r.numeroChassis());
        if(r.prochaineVT()!=null)      u.setProchaineVT(r.prochaineVT());
        if(r.capaciteVehicule()!=null) u.setCapaciteVehicule(r.capaciteVehicule());
        if(r.nbPersonnesABord()!=null) u.setNbPersonnesABord(r.nbPersonnesABord());
        if(r.proprietaireVehicule()!=null) u.setProprietaireVehicule(r.proprietaireVehicule());
        if(r.aRemorque()!=null)        u.setARemorque(r.aRemorque());
        if(r.assureurAdverse()!=null)  u.setAssureurAdverse(r.assureurAdverse());
        if(r.descriptionDegats()!=null)u.setDescriptionDegats(r.descriptionDegats());
        if(r.blessesLegers()!=null)    u.setBlessesLegers(r.blessesLegers());
        if(r.blessesGraves()!=null)    u.setBlessesGraves(r.blessesGraves());
        if(r.deces()!=null)            u.setDeces(r.deces());
        return u;
    }
}
