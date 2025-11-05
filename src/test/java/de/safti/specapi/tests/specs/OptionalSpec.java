package de.safti.specapi.tests.specs;

import de.safti.specs.annotations.Field;
import de.safti.specs.annotations.Spec;
import de.safti.specs.annotations.Structure;
import de.safti.specs.annotations.Type;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

public interface OptionalSpec extends Spec {

    @Type.StringFixed(12)
    @Structure.Optional
    @Nullable
    String nullableString();


    @Type.StringFixed(12)
    @Structure.Optional()
    Optional<String> optString();


    @Type.Int(16)
    @Structure.Optional()
    OptionalInt optInt();



    /*
     * SETTERS
     */


    @Field.Setter("nullableString")
    void setNullableString(String nullableString);

    @Field.Setter("optString")
    void setOptString(Optional<String> optString);

    @Field.Setter("optInt")
    void setOptInt(OptionalInt optInt);
}
