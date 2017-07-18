class: Workflow
cwlVersion: v1.0
inputs:
  in: string[]
outputs:
  out: string[]
steps:
  - id: one
    in:
      in: [in, two/out]
    out: [out]
    scatter: in
    run: valid.cwl.yml

  - id: two
    in:
      in: one/out
    out: [out]
    scatter: in
    run: valid.cwl.yml
