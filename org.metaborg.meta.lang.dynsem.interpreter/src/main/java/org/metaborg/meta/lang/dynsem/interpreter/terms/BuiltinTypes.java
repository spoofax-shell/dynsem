package org.metaborg.meta.lang.dynsem.interpreter.terms;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_lang.IPersistentStack;
import com.oracle.truffle.api.dsl.TypeSystem;

@TypeSystem({ IConTerm.class, ITerm.class, PersistentMap.class,
		IPersistentStack.class, String.class, int.class, boolean.class })
public class BuiltinTypes {

}