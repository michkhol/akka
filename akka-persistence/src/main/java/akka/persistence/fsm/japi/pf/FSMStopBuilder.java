/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.persistence.fsm.japi.pf;

import akka.persistence.fsm.PersistentFSM;
import akka.persistence.fsm.PersistentFSMBase;
import akka.japi.pf.FI;
import akka.japi.pf.UnitPFBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

/**
 * Builder used to create a partial function for {@link akka.actor.FSM#onTermination}.
 *
 * @param <S> the state type
 * @param <D> the data type
 *
 */
public class FSMStopBuilder<S, D> {

  private UnitPFBuilder<PersistentFSM.StopEvent<S, D>> builder =
    new UnitPFBuilder<>();

  /**
   * Add a case statement that matches on an {@link akka.actor.FSM.Reason}.
   *
   * @param reason  the reason for the termination
   * @param apply  an action to apply to the event and state data if there is a match
   * @return the builder with the case statement added
   */
  public FSMStopBuilder<S, D> stop(final PersistentFSM.Reason reason,
                                   final FI.UnitApply2<S, D> apply) {
    builder.match(PersistentFSM.StopEvent.class,
      new FI.TypedPredicate<PersistentFSM.StopEvent>() {
        @Override
        public boolean defined(PersistentFSM.StopEvent e) {
          return reason.equals(e.reason());
        }
      },
      new FI.UnitApply<PersistentFSM.StopEvent>() {
        public void apply(PersistentFSM.StopEvent e) throws Exception {
          @SuppressWarnings("unchecked")
          S s = (S) e.currentState();
          @SuppressWarnings("unchecked")
          D d = (D) e.stateData();
          apply.apply(s, d);
        }
      }
    );

    return this;
  }

  /**
   * Add a case statement that matches on a reason type.
   *
   * @param reasonType  the reason type to match on
   * @param apply  an action to apply to the reason, event and state data if there is a match
   * @param <P>  the reason type to match on
   * @return the builder with the case statement added
   */
  public <P extends PersistentFSM.Reason> FSMStopBuilder<S, D> stop(final Class<P> reasonType,
                                                          final FI.UnitApply3<P, S, D> apply) {
    return this.stop(reasonType,
      new FI.TypedPredicate<P>() {
        @Override
        public boolean defined(P p) {
          return true;
        }
      }, apply);
  }

  /**
   * Add a case statement that matches on a reason type and a predicate.
   *
   * @param reasonType  the reason type to match on
   * @param apply  an action to apply to the reason, event and state data if there is a match
   * @param predicate  a predicate that will be evaluated on the reason if the type matches
   * @param <P>  the reason type to match on
   * @return the builder with the case statement added
   */
  public <P extends PersistentFSM.Reason> FSMStopBuilder<S, D> stop(final Class<P> reasonType,
                                                          final FI.TypedPredicate<P> predicate,
                                                          final FI.UnitApply3<P, S, D> apply) {
    builder.match(PersistentFSM.StopEvent.class,
      new FI.TypedPredicate<PersistentFSM.StopEvent>() {
        @Override
        public boolean defined(PersistentFSM.StopEvent e) {
          if (reasonType.isInstance(e.reason())) {
            @SuppressWarnings("unchecked")
            P p = (P) e.reason();
            return predicate.defined(p);
          } else {
            return false;
          }
        }
      },
      new FI.UnitApply<PersistentFSM.StopEvent>() {
        public void apply(PersistentFSM.StopEvent e) throws Exception {
          @SuppressWarnings("unchecked")
          P p = (P) e.reason();
          @SuppressWarnings("unchecked")
          S s = (S) e.currentState();
          @SuppressWarnings("unchecked")
          D d = (D) e.stateData();
          apply.apply(p, s, d);
        }
      }
    );

    return this;
  }

  /**
   * Build a {@link scala.PartialFunction} from this builder.
   * After this call the builder will be reset.
   *
   * @return  a PartialFunction for this builder.
   */
  public PartialFunction<PersistentFSM.StopEvent<S, D>, BoxedUnit> build() {
    return builder.build();
  }
}
