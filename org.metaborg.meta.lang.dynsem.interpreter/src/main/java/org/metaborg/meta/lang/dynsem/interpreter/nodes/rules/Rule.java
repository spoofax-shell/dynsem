package org.metaborg.meta.lang.dynsem.interpreter.nodes.rules;

import java.util.HashSet;
import java.util.Set;

import org.metaborg.meta.lang.dynsem.interpreter.DynSemLanguage;
import org.metaborg.meta.lang.dynsem.interpreter.SourceSectionUtil;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.SourceSection;

/**
 * 
 * A node corresponding to a (merged) DynSem rule.
 * 
 * 
 * @author vladvergu
 *
 */
public class Rule extends RootNode {

	private final String constr;
	private final int arity;

	@Children protected final Premise[] premises;

	@Child protected RuleTarget target;

	public Rule(String constr, int arity, Premise[] premises,
			RuleTarget output, SourceSection source, FrameDescriptor fd) {
		super(DynSemLanguage.class, source, fd);
		this.constr = constr;
		this.arity = arity;
		this.premises = premises;
		this.target = output;
		Truffle.getRuntime().createCallTarget(this);
	}

	@ExplodeLoop
	public RuleResult execute(VirtualFrame frame) {
		/* evaluate the premises */
		for (int i = 0; i < premises.length; i++) {
			premises[i].execute(frame);
		}

		/* evaluate the rule target */
		return target.execute(frame);
	}

	public String getConstructor() {
		return constr;
	}

	public int getArity() {
		return arity;
	}

	public static Rule create(IStrategoTerm ruleT) {
		assert Tools.isTermAppl(ruleT);
		assert Tools.hasConstructor((IStrategoAppl) ruleT, "Rule", 3);

		FrameDescriptor fd = createFrameDescriptor(ruleT);

		IStrategoList premisesTerm = Tools.listAt(ruleT, 0);
		Premise[] premises = new Premise[premisesTerm.size()];
		for (int i = 0; i < premises.length; i++) {
			premises[i] = Premise.create(Tools.applAt(premisesTerm, i), fd);
		}

		IStrategoAppl relationT = Tools.applAt(ruleT, 2);
		assert Tools.hasConstructor(relationT, "Relation", 4);

		IStrategoAppl lhsConTerm = Tools.applAt(Tools.applAt(relationT, 1), 0);
		String constr = Tools.stringAt(lhsConTerm, 0).stringValue();
		int arity = Tools.listAt(lhsConTerm, 1).size();

		RuleTarget target = RuleTarget.create(Tools.applAt(relationT, 3), fd);

		return new Rule(constr, arity, premises, target,
				SourceSectionUtil.fromStrategoTerm(ruleT), fd);
	}

	private static FrameDescriptor createFrameDescriptor(IStrategoTerm t) {
		Set<String> vars = new HashSet<>();
		TermVisitor visitor = new TermVisitor() {

			@Override
			public void preVisit(IStrategoTerm t) {
				if (Tools.isTermAppl(t)
						&& Tools.hasConstructor((IStrategoAppl) t, "VarRef", 1)) {
					vars.add(Tools.stringAt(t, 0).stringValue());
				}
			}
		};

		visitor.visit(t);
		FrameDescriptor fd = FrameDescriptor.create();
		for (String v : vars) {
			fd.addFrameSlot(v);
		}
		return fd;
	}

}
