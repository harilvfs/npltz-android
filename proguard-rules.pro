# r8 rules for npltz.

# the app reflects only on framework classes (appopsmanager.checkopnothrow
# for the miui autostart check), which r8 never touches, so no -keep rules
# are needed for reflection.

# material and androidx ship their own consumer rules; views referenced from
# layouts and widget metadata get keep rules generated automatically by agp.
