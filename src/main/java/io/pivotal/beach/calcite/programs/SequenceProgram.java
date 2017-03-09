package io.pivotal.beach.calcite.programs;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.*;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.tools.Program;
import org.apache.calcite.tools.Programs;
import org.apache.calcite.util.Holder;

import java.util.List;

public class SequenceProgram implements Program {
	private final ImmutableList<Program> programs;

	public SequenceProgram(ImmutableList<Program> programs) {
		this.programs = programs;
	}

	// Use with Hook.PROGRAM.add
	@SuppressWarnings("Guava") // Must conform to Calcite's API
	public static Function<Holder<Program>, Void> prepend(Program program) {
		return (holder) -> {
			if (holder == null) {
				throw new IllegalStateException("No program holder");
			}
			Program chain = holder.get();
			if (chain == null) {
				chain = Programs.standard();
			}
			holder.set(new SequenceProgram(ImmutableList.of(program, chain)));
			return null;
		};
	}

	public RelNode run(
			RelOptPlanner planner,
			RelNode rel,
			RelTraitSet requiredOutputTraits,
			List<RelOptMaterialization> materializations,
			List<RelOptLattice> lattices
	) {
		for (Program program : programs) {
			rel = program.run(planner, rel, requiredOutputTraits, materializations, lattices);
			System.out.println("After running " + program + ":\n" + RelOptUtil.toString(rel));
		}
		return rel;
	}
}
