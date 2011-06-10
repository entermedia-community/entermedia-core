The test cases in this directory are:

no_layout.html:  Layout 1 is defined in _default.xconf, but no_layout.xconf says that no layout is to
be used.  Make sure that no layout is used.

null_layout.html:  A layout is specified, but does not exist.  Make sure that our nolayoutfound page appears
as the layout.

relative_layout.html:  A layout path is specified relative to the content path.  Verify that same layout is
returned regardless of how the path is specified (whether "./", "../", etc. is used).

nested_layout.html:  A layout file specifies a layout.  It would be nice to support nested layouts, but current
behavior of eliminating the outer layout (using only the immediately specified layout) is acceptable.
