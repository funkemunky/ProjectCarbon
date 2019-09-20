package cc.funkemunky.carbon.db;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@RequiredArgsConstructor
public class StructureSet {
    public final String id;
    public List<Structure> structures = new ArrayList<>();

    public Optional<Structure> getStructureByName(String name) {
        return structures.stream().filter(struct -> struct.name.equals(name)).findFirst();
    }

    public void removeStructure(String name) {
        structures.stream().filter(struct -> struct.name.equals(name)).forEach(structures::remove);
    }

    public void addStructure(Structure structure) {
        val optional = getStructureByName(structure.name);

        optional.ifPresent(value -> structures.remove(value));

        structures.add(structure);
    }
}
